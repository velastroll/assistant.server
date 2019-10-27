package com.percomp.assistant.core.controller.services

import com.percomp.assistant.core.config.Token
import com.percomp.assistant.core.config.newTokens
import com.percomp.assistant.core.config.oauth.InMemoryTokenStoreCustom
import com.percomp.assistant.core.dao.UserDAO
import com.percomp.assistant.core.services.CredentialRequest
import com.percomp.assistant.core.services.Tokens
import com.percomp.assistant.core.tokenStore
import io.ktor.auth.OAuth2Exception

class UserCtrl {

    suspend fun check(auth : CredentialRequest) : Token {

        // check if user exist on db
        if (auth.user.isNullOrEmpty()) throw OAuth2Exception.InvalidGrant("User not valid.")
        if (auth.password.isNullOrEmpty()) throw OAuth2Exception.InvalidGrant("Password not valid.")

        // check it on db
        UserDAO().check(auth.user, auth.password)

        // store tokens
        val tokens = newTokens(username = auth.user)

        // return it
        return tokens
    }
}
