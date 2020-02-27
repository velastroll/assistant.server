package com.percomp.assistant.core.rest

import com.percomp.assistant.core.controller.services.LocationCtrl
import com.percomp.assistant.core.controller.services.LocationService
import com.percomp.assistant.core.model.UserType
import controller.services.AuthService
import controller.services.PeopleService
import io.ktor.application.call
import io.ktor.auth.OAuth2Exception
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

fun Route.location(
    aS : AuthService,
    lS : LocationService,
    pS : PeopleService
){

    val locationCtrl = LocationCtrl(lS, pS)

    route ("worker"){
        /**
         * Creates a new location
         */
        post("towns") {
            log.info("[worker/town] Retrieving token.")
            // check authorization
            var accesstoken: String =
                call.request.headers["Authorization"] ?: throw OAuth2Exception.InvalidGrant("No token")
            accesstoken = aS.cleanTokenTag(accesstoken)
            val worker = aS.checkAccessToken(UserType.USER, accesstoken)

            log.info("[worker/town] User $worker logged.")
            val town = call.receive<TownRequest>()

            log.info("[worker/town] Town to add = $town")
            locationCtrl.add(town.name, town.postcode, town.latitude, town.longitude)

            log.info("[worker/town] Added.")
            call.respond(HttpStatusCode.OK, "Town ${town.name} successfully added.")
        }

        /**
         * This call retrieves all the locations.
         */
        get ("towns") {
            log.info("[worker/town] Retrieving token.")
            // check authorization
            var accesstoken: String =
                call.request.headers["Authorization"] ?: throw OAuth2Exception.InvalidGrant("No token")
            accesstoken = aS.cleanTokenTag(accesstoken)
            val worker = aS.checkAccessToken(UserType.USER, accesstoken)

            log.info("[worker/town] User $worker logged.")

            val locations = locationCtrl.retrieveAll()

            log.info("[worker/town] OK.")
            call.respond(HttpStatusCode.OK, locations)
        }

        /**
         * This call retrieves the location with the specified postal code
         */
        get("towns/{t}") {
            log.info("[worker/town] Retrieving token.")
            // check authorization
            var accesstoken: String =
                call.request.headers["Authorization"] ?: throw OAuth2Exception.InvalidGrant("No token")
            accesstoken = aS.cleanTokenTag(accesstoken)
            val worker = aS.checkAccessToken(UserType.USER, accesstoken)
            val postcode = call.parameters["t"]
            log.info("[worker/town] User $worker logged.")

            val location = locationCtrl.retrieve(postcode)
                ?: throw IllegalArgumentException("Does not exist location with postal code: $postcode")

            log.info("[worker/town] OK.")
            call.respond(HttpStatusCode.OK, location)
        }
    }
}

data class TownRequest (
    val name : String,
    val postcode : Int,
    val latitude : Double,
    val longitude : Double
)