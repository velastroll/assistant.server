package com.percomp.assistant.core.controller.domain

import com.percomp.assistant.core.app.config.oauth.Token
import com.percomp.assistant.core.app.config.oauth.TokenCtrl
import com.percomp.assistant.core.model.Person
import com.percomp.assistant.core.model.User
import com.percomp.assistant.core.rest.CredentialRequest
import controller.services.DeviceService
import controller.services.PeopleService
import controller.services.UserService
import io.ktor.auth.OAuth2Exception
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.lang.IllegalArgumentException

class UserCtrl : KoinComponent {


    private val userService: UserService by inject()
    private val peopleService: PeopleService by inject()
    private val deviceService: DeviceService by inject()
    private val authService : TokenCtrl by inject()


    fun check(auth : CredentialRequest) : Token {

        // check if user exist on db
        if (auth.user.isNullOrEmpty()) throw OAuth2Exception.InvalidGrant("not valid user.")
        if (auth.password.isNullOrEmpty()) throw OAuth2Exception.InvalidGrant("not valid password.")

        // check it on db
        val user = userService.check(auth.user, auth.password) ?: throw OAuth2Exception.InvalidGrant("Wrong credentials.")

        // store tokens
        val tokens = authService.newTokens(username = user.username)

        // return it
        return tokens
    }

    fun exist(username: String) : User? {
        if (username.isNullOrEmpty()) return null
        return userService.getUser(username)
    }

    fun addPerson(person: Person) {
        if (person.nif.length < 9) throw IllegalArgumentException("Not valid nif.")
        if (person.name.length < 9) throw IllegalArgumentException("Not valid name.")

        peopleService.newPerson(nif = person.nif, name = person.name, postalCode = person.postcode)
    }

    fun retrievePeople(): List<Person> {
        // return the list or an empty list
        return peopleService.getAllPeople()
    }

    /**
     * Retrieve all the people of a specific postal code with their device.
     */
    fun retrieve(postalcode: Int): ArrayList<Person> {

        // retrieve people
        val people = peopleService.getByPostalCode(postalcode) as ArrayList

        // for each person, add their device if exists
        for (p in people){
            p.relation = deviceService.getCurrentAssignmentWithUser(p.nif)
        }

        return people
    }


}
