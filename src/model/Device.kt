package com.percomp.assistant.core.model

import com.percomp.assistant.core.util.communication.RaspiAction

data class Device(
    var mac : String
){
    lateinit var password : String
    var status: String = "2000-01-01T00:00:00.000"
    var relation : Relation? = null
}

data class State(
    val device: String,
    val state: RaspiAction,
    val timestamp: String
)

data class Relation (
    var device : String? = null,
    var user: Person,
    var from: String,
    var to: String? = null
)
