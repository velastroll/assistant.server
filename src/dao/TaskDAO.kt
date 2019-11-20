package com.percomp.assistant.core.dao

import com.percomp.assistant.core.dao.DatabaseFactory.dbQuery
import com.percomp.assistant.core.domain.Events
import com.percomp.assistant.core.domain.Tasks
import com.percomp.assistant.core.model.Event
import com.percomp.assistant.core.model.Task
import com.percomp.assistant.core.util.Constants
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.security.MessageDigest

class TaskDAO {

    /**
     * Add new event in the DB.
     * @param name
     * @param content
     * @return Boolean
     **/
    suspend fun post(task: Task): Boolean = dbQuery {

        // generate id
        val id = generateId()
        Tasks.insert {
            it[Tasks.id] = id
            it[Tasks.device ] = task.device!!
            it[Tasks.by ] = task.by
            it[Tasks.at ] = task.at
            it[Tasks.event ] = task.event!!
            it[Tasks.timestamp ] = task.timestamp
        }
        return@dbQuery true

    }

    /**
     * Retrieve an event by name
     * @param name
     * @return [Event] or null
     */
    suspend fun get(device : String, from : String, to : String) : List<Task> = dbQuery{
        return@dbQuery Tasks
            .select({Tasks.device eq device and
                    (Tasks.at lessEq  to  and
                    (Tasks.timestamp greaterEq from))})
            .map {
                Task(
                    id = it[Tasks.id],
                    device=it[Tasks.device],
                    by=it[Tasks.by],
                    at=it[Tasks.at],
                    event=it[Tasks.event],
                    timestamp=it[Tasks.timestamp]
                    )
            }
    }

    private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private fun generateId() = (1..Constants.IDENTIFIER)
        .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")

}