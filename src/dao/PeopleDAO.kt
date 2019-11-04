package com.percomp.assistant.core.dao

import com.percomp.assistant.core.dao.DatabaseFactory.dbQuery
import com.percomp.assistant.core.domain.People
import com.percomp.assistant.core.domain.Users
import com.percomp.assistant.core.domain.Users.salt
import com.percomp.assistant.core.model.Person
import com.percomp.assistant.core.model.User
import com.percomp.assistant.core.util.Constants
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.security.MessageDigest

class PeopleDAO {


    /**
     * Check if the person exists
     * @param nif
     * @return [Person]
     **/
    suspend fun get(nif: String) : Person? = dbQuery {

        // to uppercase
        val nifUC = nif.toUpperCase()

        // Get a person
        People.select ({ People.nie eq nifUC }).map {
            Person(
                nif = it[People.nie],
                name = it[People.name]
            )
        }.firstOrNull()
    }

    /**
     * Create a new person.
     * @param [nif] Primary key.
     * @param [name] User name.
     */
    suspend fun post(nif: String, name: String) = dbQuery {

        val nifUC = nif.toUpperCase()

        // insert
People.insert {
            it[People.nie] = nifUC
            it[People.name] = name
        }
    }


    private fun String.sha512(): String {
        return this.hashWithAlgorithm("SHA-512")
    }

    private fun String.hashWithAlgorithm(algorithm: String): String {
        val digest = MessageDigest.getInstance(algorithm)
        val bytes = digest.digest(this.toByteArray(Charsets.UTF_8))
        return bytes.fold("") { str, it -> str + "%02x".format(it) }
    }
}