package com.percomp.assistant.core.model

import com.percomp.assistant.core.util.Constants
import com.percomp.assistant.core.util.communication.RaspiAction
import org.jetbrains.exposed.sql.Table

data class Device(
    var mac : String
){
    lateinit var password : String
    var status: ArrayList<State?> = ArrayList()
    var relation : Relation? = null
    var pending : List<Task> = ArrayList()
}

data class Relation (
    var device : String? = null,
    var user: Person?=null,
    var from: String,
    var to: String? = null,
    var position: Position? = null,
    var info : String? = null
)

data class Position(var lat : Double, var lon : Double)

data class State(
    val device: String,
    val state: RaspiAction,
    val timestamp: String,
    var content : String?
)

/** Device info for worker */

data class Device4W(
    var device: String? = null,
    var last_status: List<StateBasic>? = null,
    var last_events: List<Event4W>? = null,
    var last_intents: List<Intent4W>? = null,
    var relation: Relation? = null,
    var pending: List<Task>? = null
)

data class StateBasic(
    val type: String? = null,
    val timestamp: String
)

/** database table **/

object Devices : Table(){
    val id = varchar("id", Constants.IDENTIFIER ).primaryKey()
}

object Relations : Table() {
    val id = varchar("id", Constants.IDENTIFIER)
    val user = reference("user", People.nie)
    val device = reference("device", Devices.id)
    val from = varchar("from", Constants.TIMESTAMP)
    val to = varchar("to", Constants.TIMESTAMP).nullable()
}

object Status : Table() {
    val id = varchar("id", Constants.IDENTIFIER)
    val device = reference("device", Devices.id)
    val timestamp = varchar("timestamp", Constants.TIMESTAMP)
    val content = varchar("content", Constants.EVENT_CONTENT).nullable()
    val status = enumeration("status", RaspiAction::class)
}