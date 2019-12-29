package controller.services

import model.Intent

interface IntentsService {

    /**
     * This method stores an already done intent by a specific device.
     * The intent contains the list of involved slots.
     * @param data is the intent data. [Intent]
     */
    fun addIntentAction(data : Intent)
}