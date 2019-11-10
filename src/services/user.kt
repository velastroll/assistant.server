package com.percomp.assistant.core.services

import com.percomp.assistant.core.config.checkAccessToken
import com.percomp.assistant.core.config.cleanTokenTag
import com.percomp.assistant.core.controller.services.UserCtrl
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

fun Route.user() {


    route("worker"){

        post("login"){
            try {
                log.warn("/worker/login")
                // retrieve data
                val request = call.receive<CredentialRequest>()
                log.warn("/worker/login : request = $request")
                // check account
                val auth = UserCtrl().check(request)
                log.warn("/worker/login : auth=$auth")
                // return credentials
                call.respond(HttpStatusCode.OK, auth)
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

        post ("person"){
            try {
                log.info("[worker/person] Retrieving token.")
                // check authorization
                val accesstoken = call.request.headers["Authorization"]!!.cleanTokenTag()
                val worker = checkAccessToken(UserType.USER, accesstoken)

                log.info("[worker/person] Worker $worker try to add a new person.")
                val person = call.receive<Person>()

                log.info("[worker/person] Add person: $person")
                UserCtrl().addPerson(person)

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

        get("people") {
            try {
                // check authorization
                val accesstoken = call.request.headers["Authorization"]!!.cleanTokenTag()
                val worker = checkAccessToken(UserType.USER, accesstoken)

                // retrieve people
                val people = UserCtrl().retrievePeople()

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

data class Tokens (
    val access_token: String,
    val refresh_token : String
)

data class Response (
    val status: Int,
    val description : String
)