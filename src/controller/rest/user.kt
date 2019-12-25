package com.percomp.assistant.core.rest

import com.percomp.assistant.core.app.config.oauth.TokenCtrl
import com.percomp.assistant.core.controller.domain.UserCtrl
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
import io.ktor.server.engine.BaseApplicationResponse
import java.lang.Exception

fun Route.user(){

    val userCtrl = UserCtrl()
    val auth = TokenCtrl()

    route("worker"){

        /**
         * This call tries to log in a worker.
         */
        post("login"){
            try {
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
                    log.warn("Internal error: $e")
                    call.respond(HttpStatusCode.InternalServerError)
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
        }

        /**
         * This call tries to add a new person to the system.
         */
        post ("person"){
            try {
                log.info("[worker/person] Retrieving token.")
                // check authorization
                var accesstoken : String = call.request.headers["Authorization"] ?: throw OAuth2Exception.InvalidGrant("No token")
                accesstoken = auth.cleanTokenTag(accesstoken)
                val worker = auth.checkAccessToken(UserType.USER, accesstoken)

                log.info("[worker/person] Worker $worker try to add a new person.")
                val person = call.receive<Person>()

                log.info("[worker/person] Add person: $person")
                userCtrl.addPerson(person)

                log.info("[worker/person] Send response.")
                call.respond(HttpStatusCode.OK)
            }
            catch (e: BaseApplicationResponse.ResponseAlreadySentException){
            }
            catch(e : OAuth2Exception.InvalidGrant){
                try {
                    log.warn("Unauthorized: ${e.message}")
                    call.respond(HttpStatusCode.Unauthorized)
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
            catch(e : Exception){
                try {
                    log.warn("Internal error: ${e.message}")
                    call.respond(HttpStatusCode.InternalServerError)
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
        }

        /**
         * This method retrieves all the registered people
         */
        get("people") {
            try {
                // check authorization
                var accesstoken : String = call.request.headers["Authorization"] ?: throw OAuth2Exception.InvalidGrant("No token")
                accesstoken = auth.cleanTokenTag(accesstoken)
                val worker = auth.checkAccessToken(UserType.USER, accesstoken)

                // retrieve people
                val people =userCtrl.retrievePeople()

                // return it
                call.respond(HttpStatusCode.OK, people)
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
                    log.warn("Internal error: $e")
                    call.respond(HttpStatusCode.InternalServerError)
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
        }

    }
}



data class CredentialRequest(
    val user: String,
    val password: String
)