package com.percomp.assistant.core.rest

import com.percomp.assistant.core.app.config.oauth.TokenCtrl
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
import org.jetbrains.exposed.exceptions.ExposedSQLException
import java.lang.Exception


fun Route.location(){

    val locationCtrl = LocationCtrl()
    val auth = TokenCtrl()

    route ("worker"){
        /**
         * Creates a new location
         */
        post("towns"){
            try {
                log.info("[worker/town] Retrieving token.")
                // check authorization
                var accesstoken : String = call.request.headers["Authorization"] ?: throw OAuth2Exception.InvalidGrant("No token")
                accesstoken = auth.cleanTokenTag(accesstoken)
                val worker = auth.checkAccessToken(UserType.USER, accesstoken)

                log.info("[worker/town] User $worker logged.")
                val town = call.receive<TownRequest>()

                log.info("[worker/town] Town to add = $town")
                locationCtrl.add(town.name, town.postcode, town.latitude, town.longitude)

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
            catch(e : ExposedSQLException){
                try {
                    log.warn("[worker/towns] PSQL error: ${e.message}")
                    call.respond(HttpStatusCode.Conflict, "This post code is already in use.")
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
            catch(e : Exception){
                try {
                    log.warn("[worker/towns] Internal error: $e")
                    call.respond(HttpStatusCode.InternalServerError)
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
        }

        /**
         * This call retrieves all the locations.
         */
        get ("towns"){
            try {
                log.info("[worker/town] Retrieving token.")
                // check authorization
                var accesstoken : String = call.request.headers["Authorization"] ?: throw OAuth2Exception.InvalidGrant("No token")
                accesstoken = auth.cleanTokenTag(accesstoken)
                val worker = auth.checkAccessToken(UserType.USER, accesstoken)

                log.info("[worker/town] User $worker logged.")

                val locations = locationCtrl.retrieveAll()

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

        /**
         * This call retrieves the location with the specified postal code
         */
        get("towns/{t}"){
            try {
                log.info("[worker/town] Retrieving token.")
                // check authorization
                var accesstoken : String = call.request.headers["Authorization"] ?: throw OAuth2Exception.InvalidGrant("No token")
                accesstoken = auth.cleanTokenTag(accesstoken)
                val worker = auth.checkAccessToken(UserType.USER, accesstoken)
                val postcode = call.parameters["t"]
                log.info("[worker/town] User $worker logged.")

                val location = locationCtrl.retrieve(postcode) ?: throw IllegalArgumentException("Does not exist location with postal code: $postcode")

                log.info("[worker/town] OK.")
                call.respond(HttpStatusCode.OK, location)
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
    val postcode : Int,
    val latitude : Double,
    val longitude : Double
)