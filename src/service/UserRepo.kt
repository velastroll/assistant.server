package com.percomp.assistant.core.services

import com.percomp.assistant.core.controller.services.UserService
import com.percomp.assistant.core.model.User
import com.percomp.assistant.core.model.Users
import com.percomp.assistant.core.services.DatabaseFactory.dbQuery
import com.percomp.assistant.core.util.Constants
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.security.MessageDigest

class UserRepo : UserService {

    /**
     * This method check if the combination of user and password is correct.
     * @param id is the user identifier, which is the same than the username.
     * @param password is the secret key of the user.
     * @return user information or null.
     */
    override fun check(id: String, password: String): User? {

        if (id.isNullOrEmpty()) throw IllegalArgumentException("Checking user: the username is too short.")
        if (id.length > Constants.USERNAME) throw IllegalArgumentException("Checking user: The username is too long.")
        if (password.isNullOrEmpty()) throw IllegalArgumentException("Checking user: the password is too short.")
        if (password.length > Constants.PASSWORD) throw IllegalArgumentException("Checking user: The password is too long.")

        var salt = ""
        var pass: String? = null
        var user: User? = null

        runBlocking {
            dbQuery {
                // Get an account with this username
                Users.select { Users.username eq id }.map {
                    salt = it[Users.salt]
                    pass = it[Users.password]
                    user = User(
                        // return only this, but in a future it's possible to return more info
                        username = id
                    )
                }
            }
        }

        // Check if the account exists, or if the parameters are correct
        if (salt.isEmpty()) return null
        return if (pass != (password + salt).sha512()) null
        else user
    }

    /**
     * This method retrieves a specific user by their username.
     * @param id is the user identifier, which is the same than their username.
     * @return user information or null.
     */
    override fun getUser(id: String): User? {
        return runBlocking {
            return@runBlocking dbQuery {
                var usr: User? = null
                val usrLC = id.toLowerCase()
                // Get an account with this username
                Users.select { Users.username eq usrLC }.map {
                    usr = User(
                        username = id
                    )
                }.singleOrNull()
                return@dbQuery usr
            }
        }
    }

    /**
     * Create a new user in the database.
     * @param signUp is the form to sign up in the system.
     */
    override fun newUser(signUp: User, password: String?) {
        runBlocking {
            dbQuery {

                // crypt password
                val salt = (1..Constants.SALT)
                    .map { kotlin.random.Random.nextInt(0, Constants.CHARPOOL.size) }
                    .map(Constants.CHARPOOL::get)
                    .joinToString("")
                val newPassword = password + salt
                Users.insert {
                    it[Users.username] = signUp.username
                    // TODO: repalce by encrypter service
                    it[Users.password] = newPassword.sha512()
                    it[Users.salt] = salt
                }
            }
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