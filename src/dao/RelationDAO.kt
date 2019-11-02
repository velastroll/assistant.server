package com.percomp.assistant.core.dao

import com.percomp.assistant.core.config.backup.Logger
import com.percomp.assistant.core.dao.DatabaseFactory.dbQuery
import com.percomp.assistant.core.domain.Devices
import com.percomp.assistant.core.domain.Relation
import com.percomp.assistant.core.domain.Users
import com.percomp.assistant.core.model.Device
import com.percomp.assistant.core.model.User
import com.percomp.assistant.core.util.Constants
import io.ktor.auth.OAuth2Exception
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.joda.time.Instant
import java.security.MessageDigest

class RelationDAO {

    /**
     * Check if the combination of user and password exist on the DB.
     * @param username
     * @param password
     * @return User or null
     **/
    suspend fun check(username: String?, password: String?): User? = dbQuery {
        if (username.isNullOrEmpty()) throw IllegalArgumentException("Checking user: the username is too short.")
        if (username.length > Constants.USERNAME) throw IllegalArgumentException("Checking user: The username is too long.")
        if (password.isNullOrEmpty()) throw IllegalArgumentException("Checking user: the password is too short.")
        if (password.length > Constants.PASSWORD) throw IllegalArgumentException("Checking user: The password is too long.")

        var salt = ""
        var pass: String? = null
        var user: User? = null
        val usrLC = username.toLowerCase()

        // Get an account with this username
        Users.select { Users.username eq usrLC }.map {
            salt = it[Users.salt]
            pass = it[Users.password]
            user = User(
                // return only this, but in a future it's possible to return more info
                username = username
            )
        }

        // Check if the account exists, or if the parameters are correct
        if (salt.isNullOrEmpty()) null
        if (pass != (password + salt).sha512()) null
        else user
    }

    /**
     * Check if the user exists
     * @param username
     * @return true or false
     **/
    suspend fun checkExists(username: String): Boolean = dbQuery {

        var usr: User? = null
        val usrLC = username.toLowerCase()
        // Get an account with this username
        Users.select { Users.username eq usrLC }.map {
            usr = User(
                username = username
            )
        }
        usr != null
    }

    /**
     * Create a new user in the database.
     * @param signUp is the form to sign up in the system.
     */
    suspend fun post(username: String, device: String) = dbQuery {

        // crypt password
        val usrLC = username.toLowerCase()
        val id = (1..Constants.SALT)
            .map { kotlin.random.Random.nextInt(0, Constants.CHARPOOL.size) }
            .map(Constants.CHARPOOL::get)
            .joinToString("")
        // insert
Relation.insert {
            it[Relation.user] = usrLC
            it[Relation.device] = device
            it[Relation.id] = id
            it[Relation.from] = Instant.now().toString()
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