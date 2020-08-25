package com.percomp.assistant.core.rest

import com.percomp.assistant.core.controller.services.AuthService
import com.percomp.assistant.core.model.Token
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
fun Route.auth(authService: AuthService) {

    route("auth") {

        /**
         * Update the tokens
         */
        post("refreshtoken") {
            // get the access token
            val rt = call.receive<RefreshTokenRequest>()
            // refresh the tokens
            val tokens: Token = authService.refreshTokens(rt.refresh_token)
            // respond the new tokens
            call.respond(HttpStatusCode.OK, tokens)
        }
    }
}

data class RefreshTokenRequest (
    val refresh_token : String
)