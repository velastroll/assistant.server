package com.percomp.assistant.core.util.communication

import com.percomp.assistant.core.controller.retriever.Place


/**
 * Data class to respond specifying an error
 */
data class Response(
    val action : RaspiAction,
    var status : Int = 404,
    var data : ResponseData? = null,
    var content : String? = null
)

data class ResponseData(
    var places : List<Place>? = null
)