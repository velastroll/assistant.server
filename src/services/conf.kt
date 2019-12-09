package com.percomp.assistant.core.services

import com.percomp.assistant.core.config.checkAccessToken
import com.percomp.assistant.core.config.cleanTokenTag
import com.percomp.assistant.core.controller.services.ConfCtrl
import com.percomp.assistant.core.controller.services.DeviceCtrl
import com.percomp.assistant.core.dao.DeviceDAO
import com.percomp.assistant.core.model.ConfData
import com.percomp.assistant.core.model.UserType
import io.ktor.application.call
import io.ktor.auth.OAuth2Exception
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.server.engine.BaseApplicationResponse

fun Route.conf(){


    /* for devices */
    route("device"){

        /**
         * Responds to a specific device it last configuration data.
         */
        get("conf"){
            try {
                // check authrorization
                log.info("[device/conf] -------")
                val accesstoken = call.request.headers["Authorization"]!!.cleanTokenTag()
                val device = checkAccessToken(UserType.DEVICE, accesstoken)
                // retrieve configuration
                log.info("[device/conf] retrieving data")
                val data : ConfData = ConfCtrl().get(device) ?: throw IllegalStateException("This device has the standard configuration.")
                // respond it
                log.info("[device/conf] responding it")
                call.respond(HttpStatusCode.OK, data)
                log.info("[device/conf] OK")
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
                    log.warn("[device/conf] Internal error: $e")
                    call.respond(HttpStatusCode.InternalServerError)
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
        }

        /**
         * Device informs about the completed task of update config
         * @param timestamp identifier of the config data used to update device.
         */
        put("conf/{timestamp}"){
            try {

                log.info("[device/conf/TIMESTAMP] -------")
                // check authrorization
                val accesstoken = call.request.headers["Authorization"]!!.cleanTokenTag()
                val device = checkAccessToken(UserType.DEVICE, accesstoken)
                val timestamp = call.parameters["timestamp"] ?: throw IllegalArgumentException("No timestamp of configuration")
                log.info("[device/conf/TIMESTAMP] timestamp = ${timestamp}")
                // Mark as a done configuration
                ConfCtrl().complete(device, timestamp)
                log.info("[device/conf/TIMESTAMP] respond")
                // respond it
                call.respond(HttpStatusCode.OK, "Configuration has been updated on device.")
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
                    log.warn("[device/conf] Internal error: $e")
                    call.respond(HttpStatusCode.InternalServerError)
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
        }
    }

    /* For workers */
    route("worker"){

        /**
         * Creates a new configuration for a specific device
         */
        post("conf"){
            try {
                log.debug("[w/conf] --")
                // check authorization
                val accesstoken = call.request.headers["Authorization"]!!.cleanTokenTag()
                val worker = checkAccessToken(UserType.USER, accesstoken)
                log.debug("Access for $worker")
                // retrieve conf
                val data = call.receive<ConfData>()
                // save it
                ConfCtrl().create(configuration = data)
                // response
                call.respond(HttpStatusCode.OK)
            }
            catch (e : Exception){
                log.warn("[w/conf] Error: $e")
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

    }
}
