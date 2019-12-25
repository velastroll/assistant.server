package com.percomp.assistant.core.rest

import com.percomp.assistant.core.app.config.oauth.TokenCtrl
import com.percomp.assistant.core.controller.domain.ConfCtrl
import com.percomp.assistant.core.model.ConfData
import com.percomp.assistant.core.model.UserType
import io.ktor.application.call
import io.ktor.auth.OAuth2Exception
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.server.engine.BaseApplicationResponse
import org.koin.ktor.ext.inject

fun Route.conf(){

    val confCtrl = ConfCtrl()
    val auth = TokenCtrl()

    /* for devices */
    route("device"){

        /**
         * Responds to a specific device it last configuration data.
         */
        get("conf"){
            try {
                // check authrorization
                log.info("[device/conf] -------")
                val accesstoken = auth.cleanTokenTag(call.request.headers["Authorization"]!!)
                val device = auth.checkAccessToken(UserType.DEVICE, accesstoken)!!
                // retrieve configuration
                log.info("[device/conf] retrieving data")
                val data : ConfData = confCtrl.getActual(device)
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
         * Device informs about the completed task to update the config
         * @param timestamp identifier of the config data used to update device.
         */
        put("conf/{timestamp}"){
            try {

                log.info("[device/conf/TIMESTAMP] -------")
                // check authrorization
                val accesstoken = auth.cleanTokenTag(call.request.headers["Authorization"]!!)
                val device = auth.checkAccessToken(UserType.DEVICE, accesstoken)
                val timestamp = call.parameters["timestamp"] ?: throw IllegalArgumentException("No timestamp of configuration")
                log.info("[device/conf/TIMESTAMP] timestamp = $timestamp")
                log.info("[device/conf/TIMESTAMP] device = $device")
                // Mark as a done configuration
                confCtrl.updated(mac = device, timestamp = timestamp)
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
                val accesstoken = auth.cleanTokenTag(call.request.headers["Authorization"]!!)
                val worker = auth.checkAccessToken(UserType.DEVICE, accesstoken)
                log.debug("Access for $worker")
                // retrieve conf
                val data = call.receive<ConfData>()
                // save it
                confCtrl.new(data = data)
                // response
                call.respond(HttpStatusCode.OK, "Added new configuration")
            }
            catch (e : Exception){
                log.warn("[w/conf] Error: $e")
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        /**
         * Retrieves the configuration of a specific device
         */
        get("conf/{device}"){
            try {
                log.debug("[w/conf/{d}] --")
                // checks authorization
                val accesstoken = auth.cleanTokenTag(call.request.headers["Authorization"]!!)
                val worker = auth.checkAccessToken(UserType.DEVICE, accesstoken)
                val device = call.parameters["device"] ?: throw IllegalArgumentException("No device")
                log.debug("Access for $worker")
                // retrieves it
                val c = confCtrl.getConfs(mac = device)
                // responds it
                call.respond(HttpStatusCode.OK, c)
            }
            catch (e : Exception){
                log.warn("[w/conf({d}] Error: $e")
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

    }
}
