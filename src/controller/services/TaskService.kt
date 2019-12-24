package controller.services

import com.percomp.assistant.core.model.Event
import com.percomp.assistant.core.model.Event4W
import com.percomp.assistant.core.model.Task

interface TaskService {

    /**
     * This method creates a new type of event.
     * @param name is the new event identifier. It should be in uppercase, and the shortest possible.
     * @param content is a little description of the event.
     *
     * @return true if event was created.
     */
    fun newEventType(name : String, content : String) : Boolean

    /**
     * This method returns the info about a specific event.
     * @param name is the identifier of the event.
     * @return event info or null.
     */
    fun getEventType(name : String) : Event?

    /**
     * This method retrieves all the events.
     * @return the list of events.
     */
    fun getAllEvents() : List<Event>

    /**
     * This method retrieves the last five task done by the speicifed device.
     * @param mac is the device identifier.
     * @return the last five tasks.
     */
    fun getLastFiveTasks(mac: String): List<Event4W>

    /**
     * This method adds a new task for a specific device.
     * @param task is the task data.
     * @return true if was added.
     */
    fun newTask(task : Task) : Boolean

    /**
     * This method retrieves the list of task assigned to a specific device in a interval of dates.
     * @param device is the device identifier.
     * @param from is the min datetime to retrieve tasks.
     * @param to is the max datetime to retrieve tasks.
     */
    fun getTask(device : String, from : String, to : String = "9999-12-31T") : List<Task>

    /**
     * This method mark the task of a specific device as finished.
     * @param device is the device identifier.
     * @param typeEvent is the identifier of the finished task.
     * @param endAt is the datetime of when was finished.
     */
    fun endTask(device : String, typeEvent : String, endAt : String)

    /**
     * This method retrieves the pending task of specific device.
     * @param id is the device identifier.
     * @return list of task.
     */
    fun getPendingTaskForDevice(id : String) : List<Task>

}