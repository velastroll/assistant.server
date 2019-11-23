package com.percomp.assistant.core.dao

import com.percomp.assistant.core.dao.DatabaseFactory.dbQuery
import com.percomp.assistant.core.domain.Devices
import com.percomp.assistant.core.domain.Events
import com.percomp.assistant.core.domain.Users
import com.percomp.assistant.core.model.Device
import com.percomp.assistant.core.model.Event
import com.percomp.assistant.core.model.User
import com.percomp.assistant.core.util.Constants
import com.percomp.assistant.core.util.Constants.HEX
import com.percomp.assistant.core.util.communication.RaspiAction
import io.ktor.auth.OAuth2Exception
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import java.security.MessageDigest

class EventDAO {

    /**
     * Add new event in the DB.
     * @param name
     * @param content
     * @return Boolean
     **/
    suspend fun post(name: String, content: String): Boolean = dbQuery {

        Events.insert {
            it[Events.name] = name
            it[Events.content] = content
        }
        return@dbQuery true

    }

    /**
     * Retrieve an event by name
     * @param name
     * @return [Event] or null
     */
    suspend fun get(name : String) : Event? = dbQuery{
        return@dbQuery Events
            .select({Events.name eq name})
            .map { Event(name=it[Events.name], content=it[Events.content]) }
            .singleOrNull()
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