package com.percomp.assistant.core.model

import com.percomp.assistant.core.util.Constants
import org.jetbrains.exposed.sql.Table

data class Task(
    var id : String? = null, // task identifier
    var device : String? = null, // device identifier = mac
    var by : String? = null, // worker who send it
    var at : String? = null, // timestamp of the worker request
    var event : String? = null, // task identifier
    var timestamp: String? = "9999" // when it was confirmed
)

data class Event(
    var name : String,
    var content : String? = null
)

/** Event for worker */

data class Event4W(
    var name : String,
    var timestamp : String? = null
)

data class Intent4W(
    var timestamp: String? = null,
    var intent: String? = null,
    var slots: List<Slot4W>? = null
)

data class Slot4W(
    var slot: String? = null,
    var accuracy : Double? = null
)

/** database tables **/

object Tasks : Table(){
    val id = varchar("id", Constants.IDENTIFIER ).primaryKey()
    val device = reference("device", Devices.id )
    val by = varchar("by", Constants.IDENTIFIER).nullable()
    val at = varchar("at", Constants.TIMESTAMP).nullable()
    val event = reference("event", Events.name)
    val timestamp = varchar("timestamp", Constants.TIMESTAMP).nullable()
}

object Events : Table() {
    val name = varchar("name", Constants.NAME).primaryKey()
    var content = varchar("content", Constants.EVENT_CONTENT)
}