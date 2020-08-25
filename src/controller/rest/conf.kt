package com.percomp.assistant.core.rest

import com.percomp.assistant.core.controller.domain.ConfCtrl
import com.percomp.assistant.core.controller.services.AuthService
import com.percomp.assistant.core.controller.services.ConfService
import com.percomp.assistant.core.controller.services.DeviceService
import com.percomp.assistant.core.controller.services.LocationService
import com.percomp.assistant.core.model.ConfData
import com.percomp.assistant.core.model.UserType
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
fun Route.conf(
    auth : AuthService,
    dS : DeviceService,
    cS : ConfService,
    lS : LocationService
) {

    val confCtrl = ConfCtrl(dS, cS, lS)

    /* for devices */
    route("device"){

        /**
         * Responds to a specific device it last configuration data.
         */
        get("confs"){
            // check authrorization
            log.info("[device/confs] -------")
            val accesstoken = auth.cleanTokenTag(call.request.headers["Authorization"]!!)
            val device = auth.checkAccessToken(UserType.DEVICE, accesstoken)!!
            // retrieve configuration
            log.info("[device/confs] retrieving data")
            val data : ConfData = confCtrl.getActual(device)
            // respond it
            log.info("[device/confs] responding it")
            call.respond(HttpStatusCode.OK, data)
            log.info("[device/confs] OK")
        }

        /**
         * Device informs about the completed task to update the config
         * @param timestamp identifier of the config data used to update device.
         */
        put("confs/{timestamp}") {
            log.info("[device/confs/:TIMESTAMP] -------")
            // check authorization
            val accesstoken = auth.cleanTokenTag(call.request.headers["Authorization"]!!)
            val device = auth.checkAccessToken(UserType.DEVICE, accesstoken)
            val timestamp =
                call.parameters["timestamp"] ?: throw IllegalArgumentException("No timestamp of configuration")
            log.info("[device/confs/:TIMESTAMP] timestamp = $timestamp")
            log.info("[device/confs/:TIMESTAMP] device = $device")
            // Mark as a done configuration
            confCtrl.updated(mac = device, timestamp = timestamp)
            log.info("[device/confs/:TIMESTAMP] respond")
            // respond it
            call.respond(HttpStatusCode.OK, "Configuration has been updated on device.")
        }
    }

    /* For workers */
    route("worker"){

        /**
         * Creates a new configuration for a specific device
         */
        post("confs") {
            log.debug("[w/confs] --")
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

        /**
         * Retrieves the configuration of a specific device
         */
        get("confs/{device}") {
            log.debug("[w/confs/{d}] --")
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

    }
}

