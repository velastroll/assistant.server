package com.percomp.assistant.core.services

import com.percomp.assistant.core.controller.services.UserCtrl
import io.ktor.application.call
import io.ktor.auth.OAuth2Exception
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.server.engine.BaseApplicationResponse
import java.lang.Exception

fun Route.user() {


    route("worker"){

        post("login"){
            try {
                log.warn("/worker/login")
                // retrieve data
                val request = call.receive<CredentialRequest>()
                log.warn("/worker/login : request = $request")
                // check account
                val auth = UserCtrl().check(request)
                log.warn("/worker/login : auth=$auth")
                // return credentials
                call.respond(HttpStatusCode.OK, auth)
            }
            catch(e : OAuth2Exception.InvalidGrant){
                try {
                    log.warn("Unauthorized: $e")
                    call.respond(HttpStatusCode.Unauthorized)
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
            catch(e : Exception){
                try {
                    log.warn("Internal error: $e")
                    call.respond(HttpStatusCode.InternalServerError)
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
        }

    }
}



data class CredentialRequest(
    val user: String,
    val password: String
)

data class Tokens (
    val access_token: String,
    val refresh_token : String
)

data class Response (
    val status: Int,
    val description : String
)