package com.percomp.assistant.core.model

import com.percomp.assistant.core.util.Constants
import org.jetbrains.exposed.sql.Table


data class ConfData(
    var device : String?,
    var timestamp : String,
    var pending : Boolean = false,
    var body : ConfBody? = null
)

data class ConfBody(
    var sleep_sec : Int = 60
)

data class ConfDatas (
    var global : ConfData? = null,
    var location : ConfData? = null,
    var deviceConf : ConfData? = null,
    var pendingConf : ConfData? = null
)

/* TABLE */

object Confs : Table() {
    val receiver = varchar("receiver", Constants.IDENTIFIER)
    val timestamp = varchar("timestamp", Constants.TIMESTAMP)
    val sleep_sec = integer("sleep_sec")
    val pending = bool("pending")
}