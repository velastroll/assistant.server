package com.percomp.assistant.core.services

import com.percomp.assistant.core.config.Token
import com.percomp.assistant.core.config.refreshTokens
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.auth.OAuth2Exception
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.application
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.server.engine.BaseApplicationResponse
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
fun Route.auth() {

    val log = application.log

    route("auth") {

        /**
         * Update the tokens
         */
        post("refreshtoken") {
            try {
                // get the access token
                val rt = call.receive<RefreshTokenRequest>()
                // refresh the tokens
                val tokens: Token = refreshTokens(rt.refresh_token)
                // respond the new tokens
                call.respond(HttpStatusCode.OK, tokens)
            }
            catch (e: BaseApplicationResponse.ResponseAlreadySentException) {
            }
            catch (e: OAuth2Exception.InvalidGrant) {
                try {
                    call.respond(HttpStatusCode.Unauthorized, "${e.message}")
                }
                catch (e: BaseApplicationResponse.ResponseAlreadySentException) {
                }
            }
            catch (e: Exception) {
                log.error("/auth/refreshtoken : $e")
                try {
                    call.respond(
                        HttpStatusCode.InternalServerError,"There was a problem refreshing the token: ${e.message}")
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException) {
                }
            }
        }
    }
}

data class RefreshTokenRequest (
    val refresh_token : String
)