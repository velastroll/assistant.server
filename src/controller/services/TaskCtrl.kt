package com.percomp.assistant.core.controller.services

import com.percomp.assistant.core.dao.*
import com.percomp.assistant.core.model.Event
import com.percomp.assistant.core.model.Task
import com.percomp.assistant.core.util.Constants
import com.percomp.assistant.core.util.communication.RaspiAction
import org.joda.time.Instant

class TaskCtrl {

    suspend fun addEvent(name : String?, content: String?){
        // check values
        if (name.isNullOrEmpty()) throw IllegalArgumentException("Name cannot be empty.")
        if (name.length >= Constants.NAME) throw IllegalArgumentException("Wrong name: Length < ${Constants.NAME}.")
        if (content.isNullOrEmpty()) throw IllegalArgumentException("Content cannot be empty.")
        if (content.length >= Constants.EVENT_CONTENT) throw IllegalArgumentException("Wrong content: Length < ${Constants.EVENT_CONTENT}.")

        val state = EventDAO().post(name, content)
        if (!state) throw IllegalStateException("Event cannot be added: The name is already in use.")
    }

    suspend fun addTask(task : Task, by : String) {

        // check values
        if (task.device.isNullOrEmpty()) throw IllegalArgumentException("Device cannot be empty.")
        task.device = task.device!!
        if (DeviceCtrl().exist(task.device!!) == null) throw IllegalArgumentException("Device does not exist.")
        if (task.event.isNullOrEmpty()) throw IllegalArgumentException("Task must be a type of event.")
        EventDAO().get(task.event!!) ?: throw IllegalArgumentException("Event '${task.event}' does not exist.")
        task.at = Instant.now().toString()
        task.by = by
        TaskDAO().post(task = task)
    }

    suspend fun newStatus(device: String, status: RaspiAction, content: String? = null) : List<Task>{

        // save status
        StatusDAO().post(device, status, content)

        // check if it has pending actions
        return TaskDAO().get(device, from = Instant.now().toString())

    }

    suspend fun getAll(device: String?, from : String?, to : String) : List<Task> {

        if (device.isNullOrEmpty()) throw IllegalArgumentException("Device cannot be empty")
        if (from.isNullOrEmpty()) throw IllegalArgumentException("")
        return TaskDAO().get(device, from, to)
    }

    suspend fun done(device: String, task: String?) {

        if (task.isNullOrEmpty()) throw IllegalArgumentException("Task cannot be null.")
        val date = Instant.now().toString()
        TaskDAO().put(device, task, date)
    }

    suspend fun getEvents(): List<Event> {
        return EventDAO().getAll()
    }
}
