package com.percomp.assistant.core.rest

import com.percomp.assistant.core.controller.domain.DeviceCtrl
import com.percomp.assistant.core.controller.services.*
import com.percomp.assistant.core.model.IntentDone
import com.percomp.assistant.core.model.IntervalOfDates
import com.percomp.assistant.core.model.UserType
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.KtorExperimentalAPI
@KtorExperimentalAPI
fun Route.devices(
    aS : AuthService,
    dS : DeviceService,
    tS: TaskService,
    pS: PeopleService,
    lS: LocationService,
    iS : IntentsService
){

    val deviceCtrl = DeviceCtrl(dS, tS, pS, lS, aS, iS)

    /* for devices */
    route("device"){

        /**
         *  This call is for devices to inform about a done intent.
         */
        post("intents"){
            try {
                // get the device identification
                log.info("[devices/intents] ---- New")
                val accesstoken = aS.cleanTokenTag(call.request.headers["Authorization"]!!)
                val device : String? = aS.checkAccessToken(UserType.DEVICE, accesstoken)
                // get the intent list
                log.info("[devices/intents] Retrieving intents")
                val intent: IntentDone = call.receive()
                log.info("[devices/intents] Processing it")
                deviceCtrl.newIntentAction(device = device, intent = intent)
                log.info("[devices/intents] Ok.")
                call.respond(HttpStatusCode.OK)
                return@post
            }
            catch (e: Exception) {
                log.info("[devices/intents] Oups! : ${e}")
            }
        }

    }

    /* For workers */
    route("worker"){

        /**
         * This call retrieves all the devices.
         */
        get("devices") {
            // check authorization
            val accesstoken = aS.cleanTokenTag(call.request.headers["Authorization"]!!)
            val worker = aS.checkAccessToken(UserType.USER, accesstoken)
            log.debug("[w/devices] Access for $worker")
            // retrieve devices
            val devices = deviceCtrl.getAll()
            // respond it
            call.respond(HttpStatusCode.OK, devices)
        }

        /**
         * This call retrieves the device intents.
         */
        post("devices/{id}/intents") {
            // retrieve device identifier
            val device = call.parameters["id"] ?: throw IllegalArgumentException("No device.")
            val interval = call.receive<IntervalOfDates>()
            // retrieve
            val intents = deviceCtrl.retrieveIntents(device, interval)
            // respond
            call.respond(HttpStatusCode.OK, intents)
        }
    }
}

