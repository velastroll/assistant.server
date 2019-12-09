package com.percomp.assistant.core.model


data class ConfData(
    var device : String?,
    var timestamp : String,
    var pending : Boolean = false,
    var body : ConfBody? = null
)

data class ConfBody(
    var sleep_sec : Int = 60
)