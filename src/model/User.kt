package com.percomp.assistant.core.model

data class User(
    val username : String
){
    lateinit var password : String
}

enum class UserType{
    USER,
    DEVICE
}

data class Person(
    var name : String,
    var nif : String
)