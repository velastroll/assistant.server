package com.percomp.assistant.core.model

import com.percomp.assistant.core.util.Constants
import org.jetbrains.exposed.sql.Table

/** dataclasses */

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


data class CredentialRequest(
    val user: String,
    val password: String
)

/** database tables */

object Users : Table() {
    val username = varchar("id", Constants.USERNAME).primaryKey()
    val password = varchar("password", Constants.SHA_PASSWORD)
    val salt = varchar("salt", Constants.SALT)
}

object People : Table() {
    val nie = varchar("nie", 15).primaryKey()
    val name = varchar("name", Constants.USERNAME)
    val surname = varchar("surname", Constants.SURNAME)
    val location = reference("location", Locations.postcode)
}
