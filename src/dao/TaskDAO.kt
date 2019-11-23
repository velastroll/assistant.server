package com.percomp.assistant.core.dao

import com.percomp.assistant.core.dao.DatabaseFactory.dbQuery
import com.percomp.assistant.core.domain.Events
import com.percomp.assistant.core.domain.Tasks
import com.percomp.assistant.core.model.Event
import com.percomp.assistant.core.model.Task
import com.percomp.assistant.core.util.Constants
import org.jetbrains.exposed.sql.*
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
        }
        return@dbQuery true

    }

    /**
     * Retrieve an event by name
     * @param name
     * @return [Event] or null
     */
    suspend fun get(device : String, from : String, to : String = "9999") : List<Task> = dbQuery{
        return@dbQuery Tasks
            .select({Tasks.device eq device and
                    (Tasks.at lessEq  to  and
                    (Tasks.timestamp greaterEq from or (
                            Tasks.timestamp.isNull()
                            )))})
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

    /**
     * Finish a task setting the end date of it.
     */
    suspend fun put(device: String, task: String, date: String) = dbQuery{
        val t = Tasks.select({Tasks.device eq device and
                (Tasks.timestamp.isNull() and
                (Tasks.event eq task))
                })
            .orderBy(Tasks.at, isAsc = false)
            .map{
            Task(
                id = it[Tasks.id],
                device = it[Tasks.id],
                event = it[Tasks.event]
            )
        } as ArrayList


        if (t.isEmpty()) throw IllegalArgumentException("No task of type $task is available for $device.")

        println("Available tasks: $t")

        // update
        Tasks.update({Tasks.id eq t[0].id!!}) {
            it[Tasks.timestamp] = date
        }
    }

    private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private fun generateId() = (1..Constants.IDENTIFIER)
        .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")



}