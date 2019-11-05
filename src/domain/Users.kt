package com.percomp.assistant.core.domain

import com.percomp.assistant.core.util.Constants
import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val username = varchar("id", Constants.USERNAME).primaryKey()
    val password = varchar("password", Constants.SHA_PASSWORD)
    val salt = varchar("salt", Constants.SALT)
}

object People : Table() {
    val nie = varchar("nie", 15).primaryKey()
    val name = varchar("name", Constants.USERNAME)
}
