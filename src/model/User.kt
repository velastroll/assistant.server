package com.percomp.assistant.core.model

data class User(
    val username : String
){
    lateinit var password : String
    lateinit var name : String
}