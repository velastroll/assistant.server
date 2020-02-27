package com.percomp.assistant.core.rest

import com.percomp.assistant.core.controller.domain.DeviceCtrl
import com.percomp.assistant.core.controller.services.LocationService
import com.percomp.assistant.core.model.UserType
import controller.services.*
import io.ktor.application.call
import io.ktor.auth.OAuth2Exception
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*

fun Route.relation(
    aS : AuthService,
    dS : DeviceService,
    tS: TaskService,
    pS: PeopleService,
    lS: LocationService,
    iS : IntentsService
){
    val deviceCtrl = DeviceCtrl(dS, tS, pS, lS, aS, iS)

    /* For workers */
    route("worker") {

        /**
         * Add relation for real person
         **/
        post("relation") {
            // check authorization
            var accesstoken: String =
                call.request.headers["Authorization"] ?: throw OAuth2Exception.InvalidGrant("No token")
            accesstoken = aS.cleanTokenTag(accesstoken)
            val worker = aS.checkAccessToken(UserType.USER, accesstoken)
            val request = call.receive<RelationRequest>()
            log.error("Access for $worker")
            // add relation
            deviceCtrl.addRelation(nif = request.nif, device = request.device)
            // respond it
            call.respond(HttpStatusCode.OK, "Added relation.")
        }

        /**
         * finish relation for real person
         * **/
        delete("relation/{device}") {
            var accesstoken: String =
                call.request.headers["Authorization"] ?: throw OAuth2Exception.InvalidGrant("No token")
            accesstoken = aS.cleanTokenTag(accesstoken)
            val worker = aS.checkAccessToken(UserType.USER, accesstoken)
            // retrieve device
            val mac = call.parameters["device"] ?: throw IllegalArgumentException("No specified device.")
            // check if has any relation
            deviceCtrl.finishRelation(mac = mac)

            // respond
            call.respond(HttpStatusCode.OK, "Finished relation.")
        }

        /**
         * Retrieve the relation of a specific device.
         */
        get("relation/{device}") {
            // check authrorization
            var accesstoken: String =
                call.request.headers["Authorization"] ?: throw OAuth2Exception.InvalidGrant("No token")
            accesstoken = aS.cleanTokenTag(accesstoken)
            val worker = aS.checkAccessToken(UserType.USER, accesstoken)
            val device = call.parameters["device"] ?: throw IllegalArgumentException("Device is not specified")
            // add relation
            val relation = deviceCtrl.getRelation(device) ?: throw IllegalArgumentException("Device has not relation")
            // respond it
            call.respond(relation)
        }
    }
}

data class RelationRequest(
    var nif : String,
    var device : String
)