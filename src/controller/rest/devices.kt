package com.percomp.assistant.core.rest

import com.percomp.assistant.core.app.config.oauth.TokenCtrl
import com.percomp.assistant.core.controller.domain.DeviceCtrl
import com.percomp.assistant.core.model.UserType
import io.ktor.application.call
import io.ktor.auth.OAuth2Exception
import io.ktor.http.HttpStatusCode
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.server.engine.BaseApplicationResponse
import io.ktor.util.KtorExperimentalAPI
import model.IntentDone
import org.koin.ktor.ext.inject

@KtorExperimentalAPI
fun Route.devices(){

    val deviceCtrl = DeviceCtrl()
    val auth = TokenCtrl()

    /* for devices */
    route("devices"){

        /*
            This call is for devices to inform about a done intent.
         */
        post("intent"){
            try {
                // get the device identification
                log.info("[devices/intent] ---- New")
                val accesstoken = auth.cleanTokenTag(call.request.headers["Authorization"]!!)
                val device : String? = auth.checkAccessToken(UserType.DEVICE, accesstoken)
                // get the intent list
                log.info("[devices/intent] Retrieving intents")
                val intent: IntentDone = call.receive()
                log.info("[devices/intent] Processing it")
                deviceCtrl.newIntentAction(device = device, intent = intent)
                log.info("[devices/intent] Ok.")
                call.respond(HttpStatusCode.OK)
                return@post
            }
            catch (e: Exception) {
                log.info("Oups! : ${e}")
            }
        }

    }

    /* For workers */
    route("worker"){

        /**
         * This call retrieves all the devices.
         */
        get("devices"){
            try {
                // check authorization
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

        /**
         * This call retrieves the device intents.
         */
        post("devices/{id}/intents"){
            try {
                // retrieve device identifier
                val device = call.parameters["id"] ?: throw IllegalArgumentException("No device.")
                val interval = call.receive<IntervalOfDates>()
                // retrieve
                val intents = deviceCtrl.retrieveIntents(device, interval)
                // respond
                call.respond(HttpStatusCode.OK, intents)

            }
            catch (e : Exception) {
                log.info("Cannot retrieve stats: $e")
                call.respond(HttpStatusCode.BadRequest, "Cannot retrieve stats: $e")
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

data class IntervalOfDates(
    var from : String?,
    var to : String?
)
