package com.percomp.assistant.core.controller.services

import com.percomp.assistant.core.config.Token
import com.percomp.assistant.core.config.newTokens
import com.percomp.assistant.core.config.oauth.InMemoryTokenStoreCustom
import com.percomp.assistant.core.dao.PeopleDAO
import com.percomp.assistant.core.dao.UserDAO
import com.percomp.assistant.core.domain.People
import com.percomp.assistant.core.model.Person
import com.percomp.assistant.core.model.UserType
import com.percomp.assistant.core.services.CredentialRequest
import com.percomp.assistant.core.services.Tokens
import com.percomp.assistant.core.tokenStore
import io.ktor.auth.OAuth2Exception
import java.lang.IllegalArgumentException

class UserCtrl {

    suspend fun check(auth : CredentialRequest) : Token {

        // check if user exist on db
        if (auth.user.isNullOrEmpty()) throw OAuth2Exception.InvalidGrant("not valid user.")
        if (auth.password.isNullOrEmpty()) throw OAuth2Exception.InvalidGrant("not valid password.")

        // check it on db
        UserDAO().check(auth.user, auth.password)

        // store tokens
        val tokens = newTokens(username = auth.user)

        // return it
        return tokens
    }

    suspend fun exist(username: String) : UserType? {
        if (username.isNullOrEmpty()) return null
        if (!UserDAO().checkExists(username)) return null
        return UserType.USER
    }

    suspend fun addPerson(person: Person) {
        if (person.nif.length < 9) throw IllegalArgumentException("Not valid nif.")
        if (person.name.length < 9) throw IllegalArgumentException("Not valid name.")

        PeopleDAO().post(nif = person.nif, name = person.name)
    }

}
