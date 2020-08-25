package com.percomp.assistant.core

import app.di.myModule
import com.percomp.assistant.core.app.config.GrantAccessCtrl
import com.percomp.assistant.core.config.oauth.InMemoryIdentityCustom
import com.percomp.assistant.core.config.oauth.InMemoryTokenStoreCustom
import com.percomp.assistant.core.controller.retriever.*
import com.percomp.assistant.core.controller.services.*
import com.percomp.assistant.core.rest.*
import com.percomp.assistant.core.services.DatabaseFactory
import com.percomp.assistant.core.util.Credentials
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.*
import io.ktor.request.uri
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import nl.myndocs.oauth2.client.AuthorizedGrantType
import nl.myndocs.oauth2.client.inmemory.InMemoryClient
import nl.myndocs.oauth2.ktor.feature.Oauth2ServerFeature
import nl.myndocs.oauth2.token.converter.UUIDAccessTokenConverter
import nl.myndocs.oauth2.token.converter.UUIDRefreshTokenConverter
import nl.myndocs.oauth2.tokenstore.inmemory.InMemoryTokenStore
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
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

    // Declare Koin
    install(Koin) {
        modules(myModule)
    }

    // init database
    DatabaseFactory.init()

    // Generate instance of logger
    com.percomp.assistant.core.config.backup.Logger.instance = log

    // instance of tokenStore for OAuth authentication
    tokenStore = InMemoryTokenStoreCustom.get()


    // Install and configure the OAuth2 server //
    install(Oauth2ServerFeature) {
        tokenStore = InMemoryTokenStoreCustom.get()
    }

    // JSon converter
    install(ContentNegotiation) {
        gson()
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
        allowCredentials = true
        maxAge = Duration.ofDays(1)
    }


    /**
     * Handling call exceptions.
     * @see [Link](https://ktor.io/servers/features/status-pages.html)
     */
    install(StatusPages){
        exception<OAuth2Exception.InvalidGrant> { cause ->
            call.respond(HttpStatusCode.Unauthorized, cause.message ?: "Invalid grant.")
        }
        exception<IllegalStateException> { cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "Bad request.")
        }
        exception<IllegalArgumentException> { cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "Bad request.")
        }
        exception<Exception> { cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Internal Server Error")
        }
    }

    /**
     *  INTERCEPT THE PETITION TO CHECK OAUTH
     */
    intercept(ApplicationCallPipeline.Call){
        try {
            // Check necessary grant for this uri
            val uri = call.request.uri
            val accessToken = call.request.headers["Authorization"]
            if (!GrantAccessCtrl().checkUri(uri, accessToken)) throw OAuth2Exception.InvalidGrant("Invalid credentials")
        }
        catch (e: OAuth2Exception.InvalidGrant) {
            log.error("[Intercepted] Unauthorized.")
            call.respond(HttpStatusCode.Unauthorized, "[Intercepted] Not valid.")
            finish()
        }
        catch (e: Exception) {
            log.error("[Intercepted] Internal Server Error: $e")
            call.respond(HttpStatusCode.InternalServerError, "[Intercepted] Internal Server Error: $e")
            finish()
        }
    }


    // Execute the retriever
    IScheduledRetriever.init()


    /**
     * Inject dependencies.
     * @see [app.di.myModule]
     */
    val cS : ConfService by inject()
    val dS : DeviceService by inject()
    val iS : IntentsService by inject()
    val lS : LocationService by inject()
    val pS : PeopleService by inject()
    val tS : TaskService by inject()
    val uS : UserService by inject()
    val aS : AuthService by inject()

    /**
     * Route activation.
     * @see [controller.rest]
     */
    routing {
        auth(aS)
        basicAction(aS, dS, tS, pS, lS, iS)
        retrieve(aS)
        user(uS, pS, dS, aS)
        devices(aS, dS, tS, pS, lS, iS)
        relation(aS, dS, tS, pS, lS, iS)
        location(aS, lS, pS)
        conf(aS, dS, cS, lS)
    }
}




