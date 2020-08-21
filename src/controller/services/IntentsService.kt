package com.percomp.assistant.core.controller.services

import com.percomp.assistant.core.model.Intent
import com.percomp.assistant.core.model.Intent4W

interface IntentsService {

    /**
     * This method stores an already done intent by a specific device.
     * The intent contains the list of involved slots.
     * @param data is the intent data. [Intent]
     */
    fun addIntentAction(data : Intent)

    /**
     * This method retrieves all the intents of a specific device registered on an interval of dates.
     * @param device is the identifier of the device.
     * @param from is the min Intent datetime to retrieve.
     * @param to is the max intent datetime to retrieve.
     */
    fun getIntents(device : String, from : String, to : String) : List<Intent>

    /**
     * Retrieve the last intent of a specific device.
     * @param mac is the device identifier.
     */
    fun getLastIntent(mac: String): Intent4W?
}