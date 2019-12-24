package com.percomp.assistant.core.services

import com.percomp.assistant.core.dao.DatabaseFactory.dbQuery
import com.percomp.assistant.core.model.People
import com.percomp.assistant.core.model.Person
import controller.services.PeopleService
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import java.security.MessageDigest

class PeopleRepo : PeopleService {

    /**
     * This method retrieves the info about a person with the specified nif.
     * @param nif is the national identifier of a specific person.
     * @return the person information or null.
     */
    override fun getPerson(nif: String) : Person? {
       return runBlocking {
           return@runBlocking dbQuery {

               // Get a person
               return@dbQuery People.select({ People.nie eq nif }).map {
                   Person(
                       nif = it[People.nie],
                       name = it[People.name],
                       surname = it[People.surname],
                       postcode = it[People.location]
                   )
               }.firstOrNull()
           }
       }
    }

    /**
     * This method saves a new person on the system.
     * @param nif is the national identifier number of a specific person.
     * @param name is the user name.
     * @param postalCode is the postal code of the person location residence.
     */
    override fun newPerson(nif: String, name: String, postalCode : Int) {
        runBlocking {
            dbQuery {
                // insert
                People.insert {
                    it[People.nie] = nif
                    it[People.name] = name.split(" ")[0]
                    it[People.surname] = name.split(" ")[1]
                    it[People.location] = postalCode
                }
            }
        }
    }

    /**
     * This method retrieves the list which contains everyone who has registered on the system.
     * @return a person list.
     */
    override fun getAllPeople() : List<Person> {
        return runBlocking {
            return@runBlocking dbQuery {
                return@dbQuery People
                    .selectAll()
                    .map {
                        Person(
                            nif = it[People.nie],
                            name = it[People.name],
                            surname = it[People.surname],
                            postcode = it[People.location]
                        )
                    }
            }
        }
    }

    /**
     * This method retrieves everyone who has been registered with a specific postal code.
     * @param postalCode is the postal code of the person location residence.
     * @return a person list with the same postal code.
     */
    override fun getByPostalCode(postalCode: Int) : List<Person> {
        return runBlocking {
            return@runBlocking dbQuery {
                return@dbQuery People.select(
                    {People.location eq postalCode})
                    .map{
                        Person(
                            nif = it[People.nie],
                            name = it[People.name],
                            surname = it[People.surname],
                            postcode = it[People.location]
                        )
                    }
            }
        }
    }
}