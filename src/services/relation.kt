package com.percomp.assistant.core.services

import com.percomp.assistant.core.config.checkAccessToken
import com.percomp.assistant.core.config.cleanTokenTag
import com.percomp.assistant.core.controller.services.DeviceCtrl
import com.percomp.assistant.core.model.UserType
import io.ktor.application.call
import io.ktor.auth.OAuth2Exception
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.server.engine.BaseApplicationResponse

fun Route.relation(){


    /* for devices */
    route("relation"){

    }

    /* For workers */


    route("worker"){

        /** add relation for real person **/
        post("relation"){
            try {
                // check authrorization
                val accesstoken = call.request.headers["Authorization"]!!.cleanTokenTag()
                val worker = checkAccessToken(UserType.USER, accesstoken)
                val request = call.receive<RelationRequest>()
                log.error("Access for $worker")
                // add relation
                DeviceCtrl().addRelation(nif= request.nif, device = request.device )
                // respond it
                call.respond(HttpStatusCode.OK, "Added relation.")
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

        /** finish relation for real person **/
        delete("relation/{device}"){
            try {
                val accesstoken = call.request.headers["Authorization"]!!.cleanTokenTag()
                val worker = checkAccessToken(UserType.USER, accesstoken)
                // retrieve device
                val mac = call.parameters["device"] ?: throw IllegalArgumentException("No specified device.")
                // check if has any relation
                DeviceCtrl().finishRelation(mac = mac)

                // respond
                call.respond(HttpStatusCode.OK, "Finished relation.")
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
         * Retrieve the relation of a specific device.
         */
        get("relation/{device}"){
            try {
                // check authrorization
                val accesstoken = call.request.headers["Authorization"]!!.cleanTokenTag()
                val worker = checkAccessToken(UserType.USER, accesstoken)
                val device = call.parameters["device"] ?: throw IllegalArgumentException("Device is not specified")
                // add relation
                val relation = DeviceCtrl().getRelation(device) ?: throw IllegalArgumentException("Device has not relation")
                // respond it
                call.respond(relation)
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

data class RelationRequest(
    var nif : String,
    var device : String
)