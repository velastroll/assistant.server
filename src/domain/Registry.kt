package com.percomp.assistant.core.domain

import com.percomp.assistant.core.util.Constants
import com.percomp.assistant.core.util.communication.RaspiAction
import com.percomp.assistant.core.util.communication.Users
import org.jetbrains.exposed.sql.Table

object Registry : Table() {
    val id = varchar("id", Constants.IDENTIFIER)
    val worker = reference("worker", Users.username)
    val action = enumeration("action", RaspiAction::class)
    val to_user = reference("to_user", Users.username)
    val timestamp = varchar("timestamp", Constants.TIMESTAMP)
}