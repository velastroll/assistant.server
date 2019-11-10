package com.percomp.assistant.core.config.backup.scripts

import com.percomp.assistant.core.domain.Users
import com.percomp.assistant.core.util.Credentials
import org.jetbrains.exposed.sql.insert

fun addadmin(){
    Users.insert {
        it[Users.username] = Credentials.ADMIN_USERNAME.value
        it[Users.password] = Credentials.ADMIN_PASSWORD.value
        it[Users.salt] = Credentials.ADMIN_SALT.value
    }
}