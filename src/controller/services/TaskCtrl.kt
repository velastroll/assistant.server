package com.percomp.assistant.core.controller.services

import com.percomp.assistant.core.dao.*
import com.percomp.assistant.core.model.Task
import com.percomp.assistant.core.util.Constants
import com.percomp.assistant.core.util.communication.RaspiAction

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
        if (DeviceCtrl().exist(task.device!!) != null) throw IllegalArgumentException("Device does not exist.")
        if (by.isNullOrEmpty()) throw IllegalArgumentException("Task must be created by someone")
        if (task.at.isNullOrEmpty()) throw IllegalArgumentException("Task must have the date of it creation.")
        if (task.event.isNullOrEmpty()) throw IllegalArgumentException("Task must be a type of event.")
        task.by = by
        TaskDAO().post(task = task)
    }

    suspend fun alive(device: String) {
        StatusDAO().post(device, RaspiAction.ALIVE)
    }

    suspend fun getAll(device: String?, from : String?, to : String) : List<Task> {

        if (device.isNullOrEmpty()) throw IllegalArgumentException("Device cannot be empty")
        if (from.isNullOrEmpty()) throw IllegalArgumentException("")
        return TaskDAO().get(device, from, to)
    }
}
