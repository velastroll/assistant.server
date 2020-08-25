package com.percomp.assistant.core.rest

import com.percomp.assistant.core.controller.domain.UserCtrl
import com.percomp.assistant.core.controller.services.AuthService
import com.percomp.assistant.core.controller.services.DeviceService
import com.percomp.assistant.core.controller.services.PeopleService
import com.percomp.assistant.core.controller.services.UserService
import com.percomp.assistant.core.model.CredentialRequest
import com.percomp.assistant.core.model.Person
import com.percomp.assistant.core.model.UserType
import io.ktor.application.call
import io.ktor.auth.OAuth2Exception
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
fun Route.user(
    uS : UserService,
    pS : PeopleService,
    dS : DeviceService,
    aS : AuthService
){

    val userCtrl = UserCtrl(uS, pS, dS, aS)

    route("worker"){

        /**
         * This call tries to log in a worker.
         */
        post("login") {
            log.warn("/worker/login")
            // retrieve data
            val request = call.receive<CredentialRequest>()
            log.warn("/worker/login : request = $request")
            // check account
            val check = userCtrl.check(request)
            log.warn("/worker/login : auth=$check")
            // return credentials
            call.respond(HttpStatusCode.OK, check)
        }

        /**
         * This call tries to add a new person to the system.
         */
        post ("people") {
            log.info("[worker/people] Retrieving token.")
            // check authorization
            var accesstoken: String =
                call.request.headers["Authorization"] ?: throw OAuth2Exception.InvalidGrant("No token")
            accesstoken = aS.cleanTokenTag(accesstoken)
            val worker = aS.checkAccessToken(UserType.USER, accesstoken)

            log.info("[worker/people] Worker $worker try to add a new person.")
            val person = call.receive<Person>()

            log.info("[worker/people] Add person: $person")
            userCtrl.addPerson(person)

            log.info("[worker/people] Send response.")
            call.respond(HttpStatusCode.OK)
        }


        /**
         * This method retrieves all the registered people
         */
        get("people") {
            // check authorization
            var accesstoken: String =
                call.request.headers["Authorization"] ?: throw OAuth2Exception.InvalidGrant("No token")
            accesstoken = aS.cleanTokenTag(accesstoken)
            aS.checkAccessToken(UserType.USER, accesstoken)

            // retrieve people
            val people = userCtrl.retrievePeople()

            // return it
            call.respond(HttpStatusCode.OK, people)
        }

    }
}

