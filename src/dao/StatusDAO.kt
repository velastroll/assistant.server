package com.percomp.assistant.core.dao

import com.percomp.assistant.core.config.backup.Logger
import com.percomp.assistant.core.dao.DatabaseFactory.dbQuery
import com.percomp.assistant.core.domain.Status
import com.percomp.assistant.core.model.Event4W
import com.percomp.assistant.core.model.State
import com.percomp.assistant.core.model.State4W
import com.percomp.assistant.core.util.Constants
import com.percomp.assistant.core.util.communication.RaspiAction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.joda.time.Instant

class StatusDAO{

    /**
     * Return the last 5 status.
     */
    suspend fun getLastFive( mac : String) = dbQuery {
        return@dbQuery Status.select ({ Status.device eq mac }).orderBy( Status.timestamp, isAsc = false ).limit(5).map {
            State4W(
                type = it[Status.content] ?: it[Status.status].toString(),
                timestamp = it[Status.timestamp]
            )
        }
    }

    suspend fun post( mac : String, status : RaspiAction, content : String? = null ) = dbQuery {
        // generate id
        val id = (1..Constants.SALT)
            .map { kotlin.random.Random.nextInt(0, Constants.HEX.size) }
            .map(Constants.HEX::get)
            .joinToString("")

        Status.insert {
            it[Status.id] = id
            it[Status.device] = mac
            it[Status.timestamp] = Instant.now().toString()
            it[Status.status] = status
            it[Status.content] = content
        }
    }

    suspend fun getLastFiveTasks(mac: String): List<Event4W> = dbQuery {
        return@dbQuery Status.select ({ Status.device eq mac and (Status.content.isNotNull()) }).orderBy( Status.timestamp, isAsc = false ).limit(5).map {
            Event4W(
                timestamp = it[Status.timestamp],
                name = it[Status.content].toString()
            )
        }
    }


}
