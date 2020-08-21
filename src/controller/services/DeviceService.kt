package com.percomp.assistant.core.controller.services

import com.percomp.assistant.core.model.Device
import com.percomp.assistant.core.model.Relation
import com.percomp.assistant.core.model.StateBasic
import com.percomp.assistant.core.util.communication.RaspiAction

interface DeviceService {


    /**
     * This method checks if the combination of mac device and password is correct.
     * @param mac is the device identifier.
     * @param password is the secret device key.
     */
    fun check (mac : String, password : String) : Boolean

    /**
     * This method retrieves device info if the device exists.
     * @param mac is the device identifier.
     * @return Device information or null.
     */
    fun checkExists(mac: String) : Device?

    /**
     * Creates a new device on the system, and assign it a password.
     * @param mac is the device identifier.
     */
    fun post(mac: String)

    /**
     * This method retrieves all the devices
     * @return a list of devices.
     */
    fun getAll() : List<Device>

    /**
     * This method assigns a specific user to a specific device.
     * @param nif national identification of user who is gonna be assigned to the device.
     * @param mac is the identifier of the device.
     */
    fun assignUser(nif : String, mac : String)

    /**
     * This method retrieves the last assignment of a specific device.
     * @param mac is the device identifier.
     * @return last assignment or null.
     */
    fun getLastAssignment(mac : String) : Relation?

    /**
     * This method ends with the relation of a person with a device.
     * @param mac is the identifier of the device which is gonna be without assignment with people.
     */
    fun endAssignment(mac : String)

    /**
     * This method retrieves the current assignment of a person if exists.
     * @param nif is the national identifier of the person.
     * @return last assignment or null.
     */
    fun getCurrentAssignmentWithUser(nif : String) : Relation?

    /**
     * This method saves a new device status.
     * @param mac is the device identifier.
     * @param status is the device action.
     * @param content is a description of the task.
     */
    fun newStatus(mac : String, status : RaspiAction, content : String? = null )

    /**
     * This method retrieves the last five status of a specific device.
     * @param mac is the device identifier.
     */
    fun getLastFiveStatus(mac : String) : List<StateBasic>

    /**
     * Return the content of the last task, configured to a specific user.
     *
     * @param device is the owner of the wifi data.
     * @param task is the task of the data.
     */
    fun getContent(device : String, task : String) : String?

}