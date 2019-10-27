package com.percomp.assistant.core.domain

import com.percomp.assistant.core.util.Constants
import org.jetbrains.exposed.sql.Table

object Devices : Table(){
    val id = varchar("id", Constants.IDENTIFIER ).primaryKey()
    val salt = varchar("salt", Constants.SALT)
    val password = varchar("password", Constants.SALT)
}
