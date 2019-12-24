package com.percomp.assistant.core.dao

import com.percomp.assistant.core.dao.DatabaseFactory.dbQuery
import com.percomp.assistant.core.model.Event4W
import com.percomp.assistant.core.model.Status
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

class StatusDAO{


    suspend fun getLastFiveTasks(mac: String): List<Event4W> = dbQuery {
        return@dbQuery Status.select ({
                Status.device eq mac and (
                Status.content.isNotNull()) })
            .orderBy( Status.timestamp, isAsc = false )
            .limit(5)
            .map {
                Event4W(
                    timestamp = it[Status.timestamp],
                    name = it[Status.content].toString()
            )
        }
    }


}
