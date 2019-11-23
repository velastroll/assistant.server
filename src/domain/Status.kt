package com.percomp.assistant.core.domain

import com.percomp.assistant.core.util.Constants
import com.percomp.assistant.core.util.communication.RaspiAction
import org.jetbrains.exposed.sql.Table

object Status : Table() {
    val id = varchar("id", Constants.IDENTIFIER)
    val device = reference("device", Devices.id)
    val timestamp = varchar("timestamp", Constants.TIMESTAMP)
    val content = varchar("content", Constants.EVENT_CONTENT).nullable()
    val status = enumeration("status", RaspiAction::class)
}