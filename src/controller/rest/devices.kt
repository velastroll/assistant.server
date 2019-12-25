package com.percomp.assistant.core.rest

import com.percomp.assistant.core.app.config.oauth.TokenCtrl
import com.percomp.assistant.core.controller.domain.DeviceCtrl
import com.percomp.assistant.core.model.UserType
import io.ktor.application.call
import io.ktor.auth.OAuth2Exception
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.server.engine.BaseApplicationResponse
import org.koin.ktor.ext.inject

fun Route.devices(){

    val deviceCtrl = DeviceCtrl()
    val auth = TokenCtrl()

    /* for devices */
    route("devices"){

    }

    /* For workers */
    route("worker"){
        /**
         * This call retrieves all the devices.
         */
        get("devices"){
            try {
                // check authrorization
                val accesstoken = auth.cleanTokenTag(call.request.headers["Authorization"]!!)
                val worker = auth.checkAccessToken(UserType.USER, accesstoken)
                log.debug("[w/devices] Access for $worker")
                // retrieve devices
                val devices = deviceCtrl.getAll()
                // respond it
                call.respond(HttpStatusCode.OK, devices)
            }
            catch (e: BaseApplicationResponse.ResponseAlreadySentException){
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
                    log.warn("[w/devices] Internal error: $e")
                    call.respond(HttpStatusCode.InternalServerError)
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
        }

    }
}

data class DeviceTask(
    var device : String? = null, // device identifier = mac
    var by : String? = null, // worker who send it
    var at : String? = null, // timestamp of the worker request
    var task : String? = null, // task identifier
    var timestamp: String? = null // when it was confirmed
)