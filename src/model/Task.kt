package com.percomp.assistant.core.model

data class Task(
    var device : String? = null, // device identifier = mac
    var by : String? = null, // worker who send it
    var at : String? = null, // timestamp of the worker request
    var event : String? = null, // task identifier
    var timestamp: String? = null // when it was confirmed
)

data class Event(
    var name : String,
    var content : String? = null
)