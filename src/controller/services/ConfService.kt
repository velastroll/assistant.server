package com.percomp.assistant.core.controller.services

import com.percomp.assistant.core.model.ConfData

interface ConfService {

    /**
     * This method retrieves the current configuration data of a specific filter. Filter could be either a device identifier as a
     * postal code identifier, or "GLOBAL" to get the global configuration.
     *
     * @param filter could be either a device identifier as a postal code identifier, or "GLOBAL" to get the global configuration.
     * @param pending is the status of the configuration for individual device configuration.
     * @return the current filtered configuration.
     */
    fun get (filter : String, pending : Boolean = false) : ConfData?

    /**
     * This method saves that a specific device has been updated with the configuration which the datetime specified.
     * @param mac is the identifier of the device which has been updated.
     * @param datatime is the same datetime than the datetime of the configuration used to update the device.
     */
    fun done (mac : String, datetime : String )

    /**
     * This method creates a new configuration.
     * @param data is the data configuration.
     */
    fun post (data : ConfData)

    /**
     * This method deletes a specific configuration.
     * @param id is the device identifier of the configuration.
     * @param datetime is the datetime of the configuration.
     */
    fun delete(id : String, datetime : String)

    /**
     * This method deletes a specific configuration.
     * @param id is the device identifier of the configuration.
     * @param pending is the state of the configuration.
     */
    fun delete (id : String, pending : Boolean)
}