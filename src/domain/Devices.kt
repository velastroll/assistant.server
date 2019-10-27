package com.percomp.assistant.core.domain

import com.percomp.assistant.core.util.Constants
import org.jetbrains.exposed.sql.Table

object Devices : Table(){
    val id = varchar("id", Constants.IDENTIFIER ).primaryKey()
}
