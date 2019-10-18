package com.percomp.assistant.core

import com.percomp.assistant.core.config.Token
import com.percomp.assistant.core.config.checkUri
import com.percomp.assistant.core.config.oauth.InMemoryIdentityCustom
import com.percomp.assistant.core.config.oauth.InMemoryTokenStoreCustom
import com.percomp.assistant.core.controller.Retriever.Retriever
import com.percomp.assistant.core.controller.Retriever.Towns
import com.percomp.assistant.core.util.Credentials
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.*
import io.ktor.request.uri
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.param
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.BaseApplicationResponse
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import nl.myndocs.oauth2.client.AuthorizedGrantType
import nl.myndocs.oauth2.client.inmemory.InMemoryClient
import nl.myndocs.oauth2.ktor.feature.Oauth2ServerFeature
import nl.myndocs.oauth2.token.converter.UUIDAccessTokenConverter
import nl.myndocs.oauth2.token.converter.UUIDRefreshTokenConverter
import nl.myndocs.oauth2.tokenstore.inmemory.InMemoryTokenStore
import java.time.Duration


lateinit var tokenStore: InMemoryTokenStore


/**
 * Access to Assistant Core API.
 *
 * @author Álvaro Velasco Gil -  alvarovelascogil@gmail.com
 */
@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
fun main() {
    // Netty embedded server
    embeddedServer(
        Netty,
        watchPaths = listOf("percomp.assistant.core"),
        port = Credentials.PORT.value.toInt(),
        module = Application::coreModule
    ).apply {
        start(wait = true)
    }
}


/**
 *  CORE MODULE:
 *  Starts the routines to retrieve data and serve the API REST.
 **/
@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Application.coreModule() {


    // instance of tokenStore for OAuth authentication
    tokenStore = InMemoryTokenStoreCustom.get()

    // Install and configure the OAuth2 server //
    install(Oauth2ServerFeature) {
        identityService = InMemoryIdentityCustom()
        clientService = InMemoryClient()
            .client {
                clientId = Credentials.OAUTH_CLIENTID.value
                clientSecret = Credentials.OAUTH_CLIENTSECRET.value
                // client UrbanAir uri
                redirectUris = setOf("${Credentials.BASEURL.value}/api/conf/login")
                // set access token to half an hour
                accessTokenConverter = UUIDAccessTokenConverter(Credentials.OAUTH_ACCESSTOKEN_TIME.value.toInt())
                // set refresh token to one week
                refreshTokenConverter = UUIDRefreshTokenConverter(Credentials.OAUTH_REFRESHTOKEN_TIME.value.toInt())
                authorizedGrantTypes = setOf(
                    AuthorizedGrantType.AUTHORIZATION_CODE,
                    AuthorizedGrantType.PASSWORD,
                    AuthorizedGrantType.IMPLICIT,
                    AuthorizedGrantType.REFRESH_TOKEN
                )
            }
        tokenStore = InMemoryTokenStoreCustom.get()
    }


    // JSon converter
    install(ContentNegotiation) {
        gson {}
    }

    //install CORS
    install(CORS){
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Delete)
        method(HttpMethod.Put)
        header(HttpHeaders.AccessControlAllowOrigin)
        header(HttpHeaders.AccessControlAllowHeaders)
        header(HttpHeaders.AccessControlAllowMethods)
        header(HttpHeaders.AccessControlAllowCredentials)
        header(HttpHeaders.ContentType)
        header(HttpHeaders.AccessControlAllowCredentials)
        header(HttpHeaders.XForwardedProto)
        header(HttpHeaders.Accept)
        header("Authorization")
        header("Access-Control-Allow-Origin")
        anyHost()
        host("localhost")
        //host("lab.infor.uva.com")
        //host("lab.infor.uva.com", subDomains = listOf("TFG_"))
        //host("lab.infor.uva.com", schemes = listOf("https"))
        allowCredentials = true
        maxAge = Duration.ofDays(1)
    }

    // In this moment only test, but in un future then I put here the service modules
    routing {
        get("test"){
            val ayto = Retriever(Towns.SANVICENTEDELPALACIO).data
            println("[>] $ayto")
            call.respond(ayto)
        }
    }
}


/**
 * CLIENT MODULE:
 * Starts the client server, which access to the oauth server and configure the routes to our application.
 */
@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
fun Application.OAuthLoginApplicationWithDeps(oauthHttpClient: HttpClient) {

    // Config a different way of route to Login
    install(Locations)

    // Communication with oauth server
    install(Authentication) {
        oauth(Credentials.OAUTH_CONF.value) {
            client = HttpClient(Apache)
            providerLookup = { loginProviders[application.locations.resolve<Api>(Api::class, this).type] }
            urlProvider = { url(Api(it.name)) }
        }
    }

    /**
     *  INTERCEPT THE PETITION TO CHECK OAUTH
     **/
    intercept(ApplicationCallPipeline.Call){
        try {
            // Check necessary grant for this uri
            val uri = call.request.uri
            val accessToken = call.request.headers["Authorization"]
            if (!checkUri(uri, accessToken)) throw OAuth2Exception.InvalidGrant("Invalid credentials")
        }
        catch (e: OAuth2Exception.InvalidGrant) {
            call.respond(HttpStatusCode.Unauthorized)
        }
        catch (e: BaseApplicationResponse.ResponseAlreadySentException){}
        catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    /**
     * Client server to retrieve from the devices a couple of user-password and respond [Token].
     */
    routing {

        authenticate (Credentials.OAUTH_CONF.value) {

            // configure the oauth tokens received by client server
            location<Api> {
                param("error") {
                    handle {
                        call.respond(HttpStatusCode.Unauthorized)
                    }
                }
                handle {
                    val principal: OAuthAccessTokenResponse.OAuth2? = call.authentication.principal()
                    if (principal != null) {
                        // Authenticated
                        call.respond(Token("Bearer ${principal.accessToken}", "Bearer ${principal.refreshToken}"))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized)

                    }
                }
            }
        }
    }
}


// Client configuration to communicate with OAuth server
val loginProviders = listOf(
    OAuthServerSettings.OAuth2ServerSettings(
        name =              "login",
        authorizeUrl =      "${Credentials.BASEURL.value}/oauth/authorize",
        accessTokenUrl =    "${Credentials.BASEURL.value}/oauth/token",
        requestMethod =     HttpMethod.Post,
        clientId =          Credentials.OAUTH_CLIENTID.value,
        clientSecret =      Credentials.OAUTH_CLIENTSECRET.value,
        accessTokenRequiresBasicAuth = true
    )
).associateBy { it.name }


@KtorExperimentalLocationsAPI
@Location("/api/conf/{type?}") class Api(val type: String = "")


