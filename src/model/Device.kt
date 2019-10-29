package com.percomp.assistant.core.model

import com.percomp.assistant.core.util.communication.RaspiAction

data class Device(
    val username : String
){
    lateinit var password : String
    var status: String = "2000-01-01T00:00:00.000"
}

data class State(
    val device: String,
    val state: RaspiAction,
    val timestamp: String
)