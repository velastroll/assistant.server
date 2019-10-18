package com.percomp.assistant.core

import com.percomp.assistant.core.controller.Retriever.Retriever
import com.percomp.assistant.core.controller.Retriever.Towns
import com.percomp.assistant.core.util.Credentials
import io.ktor.application.*
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.locations.*
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import java.time.Duration

val PORT = Credentials.PORT.value

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
        port = PORT.toInt(),
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

    routing {
        get("test"){
            log.warn("Into test...")
            val ayto = Retriever(Towns.SANVICENTEDELPALACIO).data
            call.respond(ayto)
        }
    }
}


