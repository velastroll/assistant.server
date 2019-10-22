package com.percomp.assistant.core.util.communication

import com.percomp.assistant.core.util.Constants
import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val username = varchar("id", Constants.USERNAME).primaryKey()
    // val salt = varchar("salt", Constants.SALT)
    val password = varchar("password", Constants.PASSWORD)
    val name = varchar("name", Constants.NAME)
}