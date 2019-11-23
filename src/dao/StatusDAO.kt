package com.percomp.assistant.core.dao

import com.percomp.assistant.core.config.backup.Logger
import com.percomp.assistant.core.dao.DatabaseFactory.dbQuery
import com.percomp.assistant.core.domain.Status
import com.percomp.assistant.core.model.State
import com.percomp.assistant.core.util.Constants
import com.percomp.assistant.core.util.communication.RaspiAction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.joda.time.Instant

class StatusDAO{

    suspend fun get( mac : String ) = dbQuery {
        // TODO: improve this
        val s = Status.select ({ Status.device eq mac }).orderBy( Status.timestamp, isAsc = false ).map {
            State(device = it[Status.device], state = it[Status.status], timestamp = it[Status.timestamp], content = it[Status.content])
        }.firstOrNull()


        Logger.instance.info("$s")

        return@dbQuery s
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


}
