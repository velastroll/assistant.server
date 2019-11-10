package com.percomp.assistant.core.model

import com.percomp.assistant.core.controller.retriever.Towns

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
    var surname : String,
    var nif : String,
    var postcode : Int,
    var relation : Relation? = null
)