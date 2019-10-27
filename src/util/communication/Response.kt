package com.percomp.assistant.core.util.communication

import org.json.JSONArray


/**
 * Data class to respond specifying an error
 */
data class Response(
    val action : RaspiAction,
    var status : String?,
    var config : JSONArray
)