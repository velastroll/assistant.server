package com.percomp.assistant.core.services

import com.percomp.assistant.core.config.checkAccessToken
import com.percomp.assistant.core.config.cleanTokenTag
import com.percomp.assistant.core.controller.services.LocationCtrl
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


fun Route.location(){

    route ("worker"){
        post("towns"){
            try {
                log.info("[worker/town] Retrieving token.")
                // check authorization
                var accesstoken : String = call.request.headers["Authorization"] ?: throw OAuth2Exception.InvalidGrant("No token")
                accesstoken = accesstoken.cleanTokenTag()
                val worker = checkAccessToken(UserType.USER, accesstoken)

                log.info("[worker/town] User $worker logged.")
                val town = call.receive<TownRequest>()

                log.info("[worker/town] Town to add = $town")
                LocationCtrl().add(town.name, town.postcode)

                log.info("[worker/town] Added.")
                call.respond(HttpStatusCode.OK, "Town ${town.name} successfully added.")
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
            catch(e : IllegalArgumentException){
                try {
                    log.warn("[worker/towns] Cannot add the town: ${e.message}")
                    call.respond(HttpStatusCode.BadRequest)
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

        get ("towns"){
            try {
                log.info("[worker/town] Retrieving token.")
                // check authorization
                var accesstoken : String = call.request.headers["Authorization"] ?: throw OAuth2Exception.InvalidGrant("No token")
                accesstoken = accesstoken.cleanTokenTag()
                val worker = checkAccessToken(UserType.USER, accesstoken)

                log.info("[worker/town] User $worker logged.")

                val locations = LocationCtrl().retrieveAll()

                log.info("[worker/town] OK.")
                call.respond(HttpStatusCode.OK, locations)
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
            catch(e : IllegalArgumentException){
                try {
                    log.warn("[worker/towns] Cannot get towns: ${e.message}")
                    call.respond(HttpStatusCode.BadRequest)
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

data class TownRequest (
    val name : String,
    val postcode : Int
)