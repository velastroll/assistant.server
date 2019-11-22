package com.percomp.assistant.core.domain

import com.percomp.assistant.core.util.Constants
import org.jetbrains.exposed.sql.Table

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