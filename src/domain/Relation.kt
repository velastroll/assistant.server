package com.percomp.assistant.core.domain

import com.percomp.assistant.core.util.Constants
import com.percomp.assistant.core.util.communication.Users
import org.jetbrains.exposed.sql.Table

object Relation : Table() {
    val id = varchar("id", Constants.IDENTIFIER)
    val user = reference("user", Users.username)
    val device = reference("device", Devices.id)
}