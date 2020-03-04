package com.percomp.assistant.core.controller.domain

import com.percomp.assistant.core.model.Event
import com.percomp.assistant.core.model.Task
import com.percomp.assistant.core.util.Constants
import com.percomp.assistant.core.util.communication.RaspiAction
import controller.services.DeviceService
import controller.services.TaskService
import org.joda.time.Instant

class TaskCtrl (

    val deviceService: DeviceService,
    val taskService: TaskService ) {


    fun addEvent(name : String?, content: String?){
        // check values
        if (name.isNullOrEmpty()) throw IllegalArgumentException("Name cannot be empty.")
        if (name.length >= Constants.NAME) throw IllegalArgumentException("Wrong name: Length < ${Constants.NAME}.")
        if (content.isNullOrEmpty()) throw IllegalArgumentException("Content cannot be empty.")
        if (content.length >= Constants.EVENT_CONTENT) throw IllegalArgumentException("Wrong content: Length < ${Constants.EVENT_CONTENT}.")

        val state = taskService.newEventType(name, content)
        if (!state) throw IllegalStateException("Event cannot be added: The name is already in use.")
    }

    fun addTask(task : Task, by : String) {

        // check values
        if (task.device.isNullOrEmpty()) throw IllegalArgumentException("Device cannot be empty.")
        if (task.event.isNullOrEmpty()) throw IllegalArgumentException("Task must be a type of event.")
        taskService.getEventType(task.event!!) ?: throw IllegalArgumentException("Event '${task.event}' does not exist.")

        // send tasks
        if(task.device == "GLOBAL"){
            // global petition, send petition for all devices
            val ds = deviceService.getAll()
            for (d in ds){
                task.device = d.mac
                task.at = Instant.now().toString()
                task.by = by
                taskService.newTask(task = task)
            }
        } else if (task.device!!.length < 10){
            // location: send petition for all location devices
            val ds = deviceService.getAll().filter { d -> d.relation != null && d.relation!!.user!!.postcode.toString() == task.device }
            for (d in ds){
                task.device = d.mac
                task.at = Instant.now().toString()
                task.by = by
                taskService.newTask(task = task)
            }

        } else {
            // device
            if (deviceService.checkExists(task.device!!) == null) throw IllegalArgumentException("Device does not exist.")
            task.at = Instant.now().toString()
            task.by = by
            taskService.newTask(task = task)
        }
    }

    fun newStatus(device: String, status: RaspiAction, content: String? = null) : List<Task>{

        // save status
        deviceService.newStatus(device, status, content)

        // check if it has pending actions
        return taskService.getPendingTaskForDevice(device)

    }

    fun getAll(device: String?, from : String?, to : String) : List<Task> {

        if (device.isNullOrEmpty()) throw IllegalArgumentException("Device cannot be empty")
        if (from.isNullOrEmpty()) throw IllegalArgumentException("")
        return taskService.getTask(device, from, to)
    }

    fun done(device: String, task: String?) : String? {

        if (task.isNullOrEmpty()) throw IllegalArgumentException("Task cannot be null.")
        val date = Instant.now().toString()
        taskService.endTask(device, task, date)

        // retrieve related content to the task
        return deviceService.getContent(device, task)
    }

    fun getEvents(): List<Event> {
        return taskService.getAllEvents()
    }
}
