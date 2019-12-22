package com.percomp.assistant.core.controller.services

import com.percomp.assistant.core.config.Token
import com.percomp.assistant.core.config.newTokens
import com.percomp.assistant.core.dao.PeopleDAO
import com.percomp.assistant.core.dao.RelationDAO
import com.percomp.assistant.core.dao.UserDAO
import com.percomp.assistant.core.model.Person
import com.percomp.assistant.core.model.User
import com.percomp.assistant.core.services.CredentialRequest
import io.ktor.auth.OAuth2Exception
import java.lang.IllegalArgumentException

class UserCtrl {

    suspend fun check(auth : CredentialRequest) : Token {

        // check if user exist on db
        if (auth.user.isNullOrEmpty()) throw OAuth2Exception.InvalidGrant("not valid user.")
        if (auth.password.isNullOrEmpty()) throw OAuth2Exception.InvalidGrant("not valid password.")

        // check it on db
        val user = UserDAO().check(auth.user, auth.password) ?: throw OAuth2Exception.InvalidGrant("Wrong credentials.")

        // store tokens
        val tokens = newTokens(username = user.username)

        // return it
        return tokens
    }

    suspend fun exist(username: String) : User? {
        if (username.isNullOrEmpty()) return null
        return UserDAO().checkExists(username)
    }

    suspend fun addPerson(person: Person) {
        if (person.nif.length < 9) throw IllegalArgumentException("Not valid nif.")
        if (person.name.length < 9) throw IllegalArgumentException("Not valid name.")

        PeopleDAO().post(nif = person.nif, name = person.name, postcode = person.postcode)
    }

    suspend fun retrievePeople(): List<Person> {
        // return the list or an empty list
        return PeopleDAO().getAll() ?: ArrayList()
    }

    /**
     * Retrieve all the people of a specific postal code with their device.
     */
    suspend fun retrieve(postalcode: Int): ArrayList<Person> {

        // retrieve people
        val people = UserDAO().retrieve(postalcode)

        // for each person, add their device if exists
        for (p in people){
            p.relation = RelationDAO().getCurrentByUser(user = p.nif)
        }

        return people
    }


}
