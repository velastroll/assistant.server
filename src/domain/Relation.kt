package com.percomp.assistant.core.domain

import com.percomp.assistant.core.util.Constants
import io.netty.util.Constant
import org.jetbrains.exposed.sql.Table

object Relation : Table() {
    val id = varchar("id", Constants.IDENTIFIER)
    val user = reference("user", Users.username)
    val device = reference("device", Devices.id)
    val from = varchar("from", Constants.TIMESTAMP)
    val to = varchar("to", Constants.TIMESTAMP).nullable()
}