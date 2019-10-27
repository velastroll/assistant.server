package com.percomp.assistant.core

import com.percomp.assistant.core.util.communication.RaspiAction
import com.percomp.assistant.core.util.communication.Response
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route

fun Route.alive(){
    // need to check if any function is ready to send to the device
    route("alive"){
        post(){
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