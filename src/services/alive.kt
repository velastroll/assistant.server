package com.percomp.assistant.core

import com.percomp.assistant.core.controller.services.DeviceCtrl
import com.percomp.assistant.core.services.CredentialRequest
import com.percomp.assistant.core.services.log
import com.percomp.assistant.core.util.communication.RaspiAction
import com.percomp.assistant.core.util.communication.Response
import io.ktor.application.call
import io.ktor.auth.OAuth2Exception
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.server.engine.BaseApplicationResponse

fun Route.alive(){
    // need to check if any function is ready to send to the device
    route("device"){
        post("login"){
            try {
                log.warn("/device/login")
                // retrieve data
                val request = call.receive<CredentialRequest>()
                log.warn("/device/login : request = $request")
                // check account
                val auth = DeviceCtrl().check(request)
                log.warn("/device/login : auth=$auth")
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

        post("alive"){
            // TODO: extract device from Auth

            // TODO: save state on DB

            // TODO: check pending actions

            // TODO: reply
            call.respond(HttpStatusCode.OK, Response(status = 200, action = RaspiAction.ALIVE))
        }
    }
}

data class Request (
    var status : Int = 404,
    var action : RaspiAction? = null,
    var data : String? = null
)