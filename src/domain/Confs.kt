package com.percomp.assistant.core.domain

import com.percomp.assistant.core.util.Constants
import com.percomp.assistant.core.util.communication.RaspiAction
import org.jetbrains.exposed.sql.Table

object Confs : Table() {
    val device = reference("device", Devices.id)
    val timestamp = varchar("timestamp", Constants.TIMESTAMP)
    val sleep_sec = integer("sleep_sec")
    val pending = bool("pending")
}