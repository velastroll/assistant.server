package com.percomp.assistant.core.services

import com.percomp.assistant.core.dao.DatabaseFactory.dbQuery
import com.percomp.assistant.core.model.*
import com.percomp.assistant.core.util.Constants
import controller.services.TaskService
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*

class TaskRepo : TaskService {

    /**
     * This method creates a new type of event.
     * @param name is the new event identifier. It should be in uppercase, and the shortest possible.
     * @param content is a little description of the event.
     *
     * @return true if event was created.
     */
    override fun newEventType(name: String, content: String): Boolean {
        return runBlocking {
            return@runBlocking dbQuery {
                Events.insert {
                    it[Events.name] = name
                    it[Events.content] = content
                }
                return@dbQuery true
            }
        }
    }

    /**
     * This method returns the info about a specific event.
     * @param name is the identifier of the event.
     * @return event info or null.
     */
    override fun getEventType(name : String) : Event? {
        return runBlocking {
            return@runBlocking dbQuery {
                return@dbQuery Events
                    .select({Events.name eq name})
                    .map { Event(name=it[Events.name], content=it[Events.content]) }
                    .singleOrNull()
            }
        }
    }

    /**
     * This method retrieves all the events.
     * @return the list of events.
     */
    override fun getAllEvents() : List<Event> {
        return runBlocking {
            return@runBlocking dbQuery {
                return@dbQuery Events.selectAll().map {
                    Event(
                        name = it[Events.name],
                        content = it[Events.content]
                    )
                } as ArrayList
            }
        }
    }

    /**
     * This method adds a new task for a specific device.
     * @param task is the task data.
     * @return true if was added.
     */
    override fun newTask(task: Task): Boolean{
        return runBlocking {
            return@runBlocking dbQuery {
                // generate id
                val id = generateId()
                Tasks.insert {
                    it[Tasks.id] = id
                    it[Tasks.device] = task.device!!
                    it[Tasks.by] = task.by
                    it[Tasks.at] = task.at
                    it[Tasks.event] = task.event!!
                    it[Tasks.content] = task.content
                }
                return@dbQuery true
            }
        }
    }

    /**
     * This method retrieves the list of task assigned to a specific device in a interval of dates.
     * @param device is the device identifier.
     * @param from is the min datetime to retrieve tasks.
     * @param to is the max datetime to retrieve tasks.
     */
    override fun getTask(device : String, from : String, to : String) : List<Task> {
        return runBlocking {
            return@runBlocking dbQuery {
                return@dbQuery Tasks
                    .select({
                        Tasks.device eq device and
                        (Tasks.at lessEq to and
                        (Tasks.timestamp greaterEq from or (
                        Tasks.timestamp.isNull())))})
                    .map {
                        Task(
                            id = it[Tasks.id],
                            device = it[Tasks.device],
                            by = it[Tasks.by],
                            at = it[Tasks.at],
                            event = it[Tasks.event],
                            timestamp = it[Tasks.timestamp],
                            content = it[Tasks.content]
                        )
                    }
            }
        }
    }

    /**
     * This method mark the task of a specific device as finished.
     * @param device is the device identifier.
     * @param typeEvent is the identifier of the finished task.
     * @param endAt is the datetime of when was finished.
     */
    override fun endTask(device: String, typeEvent: String, endAt: String) {
        runBlocking {
            dbQuery {
                val t = Tasks.select({
                    Tasks.device eq device and
                            (Tasks.timestamp.isNull() and
                                    (Tasks.event eq typeEvent))
                })
                    .orderBy(Tasks.at, isAsc = false)
                    .map {
                        Task(
                            id = it[Tasks.id],
                            device = it[Tasks.id],
                            event = it[Tasks.event]
                        )
                    } as ArrayList


                if (t.isEmpty()) throw IllegalArgumentException("No task of type $typeEvent is available for $device.")

                println("Available tasks: $t")

                // update
                Tasks.update({ Tasks.id eq t[0].id!! }) {
                    it[Tasks.timestamp] = endAt
                }
            }
        }
    }

    /**
     * This method retrieves the pending task of specific device.
     * @param id is the device identifier.
     * @return list of task.
     */
    override fun getPendingTaskForDevice(id: String) : List<Task> {
        return runBlocking {
            return@runBlocking dbQuery {
                return@dbQuery Tasks
                    .select({
                        Tasks.device eq id and
                        (Tasks.timestamp.isNull())})
                    .map {
                        Task(
                            id = it[Tasks.id],
                            device = it[Tasks.device],
                            event = it[Tasks.event],
                            by = it[Tasks.by],
                            timestamp = it[Tasks.timestamp],
                            at = it[Tasks.at],
                            content = it[Tasks.content]
                        )
                    } as ArrayList
            }
        }
    }

    /**
     * This method retrieves the last five task done by the speicifed device.
     * @param mac is the device identifier.
     * @return the last five tasks.
     */
    override fun getLastFiveTasks(mac: String): List<Event4W> {
        return runBlocking {
            return@runBlocking dbQuery {
                return@dbQuery Status
                    .select({
                        Status.device eq mac and (
                        Status.content.isNotNull())})
                    .orderBy(Status.timestamp, isAsc = false)
                    .limit(5)
                    .map {
                        Event4W(
                            timestamp = it[Status.timestamp],
                            name = it[Status.content].toString()
                        )
                    }
            }
        }
    }

    /** ------- */

    private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private fun generateId() = (1..Constants.IDENTIFIER)
        .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")

}