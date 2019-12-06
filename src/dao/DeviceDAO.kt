package com.percomp.assistant.core.dao

import com.percomp.assistant.core.dao.DatabaseFactory.dbQuery
import com.percomp.assistant.core.domain.Devices
import com.percomp.assistant.core.domain.Status
import com.percomp.assistant.core.domain.Users
import com.percomp.assistant.core.model.Device
import com.percomp.assistant.core.model.State
import com.percomp.assistant.core.model.User
import com.percomp.assistant.core.util.Constants
import com.percomp.assistant.core.util.Constants.HEX
import com.percomp.assistant.core.util.communication.RaspiAction
import io.ktor.auth.OAuth2Exception
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import java.security.MessageDigest

class DeviceDAO {

    /**
     * Check if the combination of user and password exist on the DB.
     * @param mac
     * @param password
     * @return Boolean
     **/
    suspend fun check(mac: String?, password: String?): Boolean = dbQuery {
        if (mac.isNullOrEmpty()) throw IllegalArgumentException("Checking user: the username is too short.")
        if (mac.length > 17) throw IllegalArgumentException("Checking user: The username is too long.")
        if (password.isNullOrEmpty()) throw IllegalArgumentException("Checking user: the password is too short.")

        // If the combination is right return true, and create a new device if does not exist yet.
        if (decode(password).equals(mac)){
            runBlocking {
                if (checkExists(mac) == null){
                    DeviceDAO().post(mac = mac)
                }
            }
            runBlocking {
                // update their status
                StatusDAO().post(mac, RaspiAction.LOGIN)
            }
            return@dbQuery true
        }
        else {
            false
        }
    }

    /**
     * Check if the user exists
     * @param mac
     * @return true or false
     **/
    suspend fun checkExists(mac: String): Device? = dbQuery {

        var device: Device? = null
        // Get an account with this username
        Devices.select { Devices.id eq mac }.map {
            device = Device(
                mac = mac
            )
        }.singleOrNull()
        device
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

    /**
     * Retrieve all the devices.
     */
    suspend fun getAll(): List<Device> = dbQuery{
        Devices.selectAll().map{
            Device(mac = it[Devices.id])
        }
    }

    private fun decode(password: String) : String {
        if (password.length != 19) throw OAuth2Exception.InvalidGrant("Invalid credentials.")

        var new = ""
        for (c in password){
            if (HEX.contains(c)) new = "$new$c"
        }
        val s =  new.substring(8, 10) + ":" + new.substring(4,6) + ":" + new.substring(10,12) + ":"+ new.substring(0, 2) + ":" + new.substring(6, 8) + ":" + new.substring(2,4)
        return s
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