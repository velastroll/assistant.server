package com.percomp.assistant.core.dao

import com.percomp.assistant.core.dao.DatabaseFactory.dbQuery
import com.percomp.assistant.core.domain.Devices
import com.percomp.assistant.core.domain.Users
import com.percomp.assistant.core.model.Device
import com.percomp.assistant.core.model.User
import com.percomp.assistant.core.util.Constants
import com.percomp.assistant.core.util.Constants.HEX
import io.ktor.auth.OAuth2Exception
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.security.MessageDigest

class DeviceDAO {

    /**
     * Check if the combination of user and password exist on the DB.
     * @param username
     * @param password
     * @return Boolean
     **/
    suspend fun check(username: String?, password: String?): Boolean = dbQuery {
        if (username.isNullOrEmpty()) throw IllegalArgumentException("Checking user: the username is too short.")
        if (username.length > 17) throw IllegalArgumentException("Checking user: The username is too long.")
        if (password.isNullOrEmpty()) throw IllegalArgumentException("Checking user: the password is too short.")

        var salt = ""
        var pass: String? = null
        var device: Device? = null
        val id = username.toLowerCase()

        // Get an account with this username
        Devices.select { Devices.id eq id }.map {
            device = Device(
                // return only this, but in a future it's possible to return more info
                username = username
            )
        }

        if (decode(password).equals(username)) return@dbQuery true
        else false
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
     * @param mac is the MAC direction of the device
     */
    suspend fun post(mac : String) = dbQuery {
        Devices.insert {
            it[Devices.id] = mac
        }
    }

    private fun decode(password: String) : String {
        if (password.length != 19) throw OAuth2Exception.InvalidGrant("Invalid credentials.")

        var new = ""
        for (c in password){
            if (HEX.contains(c)) new = "$new$c"
        }
        return new.substring(6,8) + new.substring(10,12) + new.substring(2,4) + new.substring(8, 10) + new.substring(0, 2) + new.substring(4,6)
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