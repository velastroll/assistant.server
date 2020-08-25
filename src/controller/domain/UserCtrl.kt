package com.percomp.assistant.core.controller.domain

import com.percomp.assistant.core.controller.services.AuthService
import com.percomp.assistant.core.controller.services.DeviceService
import com.percomp.assistant.core.controller.services.PeopleService
import com.percomp.assistant.core.controller.services.UserService
import com.percomp.assistant.core.model.CredentialRequest
import com.percomp.assistant.core.model.Person
import com.percomp.assistant.core.model.Token
import com.percomp.assistant.core.model.User
import io.ktor.auth.OAuth2Exception
import io.ktor.util.KtorExperimentalAPI
import java.lang.IllegalArgumentException



@KtorExperimentalAPI
class UserCtrl (
    private val userService: UserService,
    private val peopleService: PeopleService,
    private val deviceService: DeviceService,
    private val authService : AuthService
){

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
