package com.percomp.assistant.core.services

import com.percomp.assistant.core.config.checkAccessToken
import com.percomp.assistant.core.config.cleanTokenTag
import com.percomp.assistant.core.controller.services.DeviceCtrl
import com.percomp.assistant.core.dao.DeviceDAO
import com.percomp.assistant.core.model.UserType
import io.ktor.application.call
import io.ktor.auth.OAuth2Exception
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.server.engine.BaseApplicationResponse

fun Route.devices(){


    /* for devices */
    route("devices"){

    }

    /* For workers */
    route("worker"){
        get("devices"){
            try {
                // check authrorization
                val accesstoken = call.request.headers["Authorization"]!!.cleanTokenTag()
                val worker = checkAccessToken(UserType.USER, accesstoken)
                log.error("Access for $worker")
                // retrieve devices
                val devices = DeviceCtrl().getAll()
                // respond it
                call.respond(HttpStatusCode.OK, devices)
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