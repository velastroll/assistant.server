package com.percomp.assistant.core.model

import com.percomp.assistant.core.util.communication.RaspiAction

data class Device(
    var mac : String
){
    lateinit var password : String
    var status: ArrayList<State?> = ArrayList()
    var relation : Relation? = null
    var pending : List<Task> = ArrayList()
}

/**
 * Device info for worker
 */
data class Device4W(
    var device: String? = null,
    var last_status: List<State4W>? = null,
    var last_events: List<Event4W>? = null,
    var last_intents: List<Intent4W>? = null,
    var relation: Relation? = null,
    var pending: List<Task>? = null
)

data class Position(var lat : Double, var lon : Double)

data class State(
    val device: String,
    val state: RaspiAction,
    val timestamp: String,
    var content : String?
)

// State for worker
data class State4W(
    val type: String? = null,
    val timestamp: String
)

data class Relation (
    var device : String? = null,
    var user: Person?=null,
    var from: String,
    var to: String? = null,
    var position: Position? = null,
    var info : String? = null
)