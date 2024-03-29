package com.percomp.assistant.core.rest

import com.percomp.assistant.core.config.backup.Logger
import com.percomp.assistant.core.controller.retriever.IScheduledRetriever
import com.percomp.assistant.core.controller.retriever.Towns
import com.percomp.assistant.core.controller.services.AuthService
import com.percomp.assistant.core.model.UserType
import com.percomp.assistant.core.util.communication.RaspiAction
import com.percomp.assistant.core.util.communication.Response
import com.percomp.assistant.core.util.communication.ResponseData
import io.ktor.application.call
import io.ktor.auth.OAuth2Exception
import io.ktor.features.origin
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.util.KtorExperimentalAPI

val log = Logger.instance

@KtorExperimentalAPI
fun Route.retrieve(
    auth : AuthService
){

    // need to check if any function is ready to send to the device
    route("towns/{town}"){
        get{
            // TODO: extract device from Auth
            var accesstoken : String = call.request.headers["Authorization"] ?: throw OAuth2Exception.InvalidGrant("No token")
            accesstoken = auth.cleanTokenTag(accesstoken)
            auth.checkAccessToken(UserType.USER, accesstoken)
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
        }
    }
}
