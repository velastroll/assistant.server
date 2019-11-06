package com.percomp.assistant.core.services

import com.percomp.assistant.core.config.backup.Logger
import com.percomp.assistant.core.config.checkAccessToken
import com.percomp.assistant.core.config.cleanTokenTag
import com.percomp.assistant.core.controller.retriever.IScheduledRetriever
import com.percomp.assistant.core.controller.retriever.Towns
import com.percomp.assistant.core.model.UserType
import com.percomp.assistant.core.util.communication.RaspiAction
import com.percomp.assistant.core.util.communication.Response
import com.percomp.assistant.core.util.communication.ResponseData
import io.ktor.application.call
import io.ktor.features.origin
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route


val log = Logger.instance

fun Route.retrieve(){
    // need to check if any function is ready to send to the device
    route("towns/{town}"){
        post(){
            try {
                // TODO: extract device from Auth
                val accesstoken = call.request.headers["Authorization"]!!.cleanTokenTag()
                val worker = checkAccessToken(UserType.USER, accesstoken)
                // retrieve the data
                val town : String = call.parameters["town"] ?: ""
                val places = IScheduledRetriever.get(Towns.valueOf(town.toUpperCase()))
                log.debug("[${call.request.origin.remoteHost} : /towns/$town")

                // TODO: check pending actions
                log.debug("TODO: check pending actions")

                // Generate response
                val response = Response(status = 200, action = RaspiAction.ALIVE)
                    response.data = ResponseData(places = places)
                log.debug("Response: $response")
                // Reply
                call.respond(HttpStatusCode.OK, response)

            } catch (e : Exception){
                call.respond(HttpStatusCode.Conflict, Response(status = 501, action = RaspiAction.ALIVE))
            }
        }
    }
}
