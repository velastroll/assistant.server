package com.percomp.assistant.core.controller.services

import com.percomp.assistant.core.config.Token
import com.percomp.assistant.core.config.newTokens
import com.percomp.assistant.core.dao.DeviceDAO
import com.percomp.assistant.core.model.UserType
import com.percomp.assistant.core.services.CredentialRequest
import io.ktor.auth.OAuth2Exception

class DeviceCtrl {

    suspend fun check(auth : CredentialRequest) : Token {

        // check if user exist on db
        if (auth.user.isNullOrEmpty()) throw OAuth2Exception.InvalidGrant("Not valid user")
        if (auth.password.isNullOrEmpty()) throw OAuth2Exception.InvalidGrant("Not valid password")

        // check it on db
        DeviceDAO().check(auth.user, auth.password)

        // store tokens
        val tokens = newTokens(username = auth.user)

        // return it
        return tokens
    }

    suspend fun exist(mac: String) : UserType? {
        if (mac.isNullOrEmpty()) return null
        if (!DeviceDAO().checkExists(mac)) return null
        return UserType.DEVICE
    }

    suspend fun create(mac : String){
        // check if user exist on db
        if (mac.isNullOrEmpty()) throw IllegalArgumentException("Invalid user")
        if (mac.length != 17 ) throw IllegalArgumentException("Invalid user")
        if (!mac.contains(":")) throw IllegalArgumentException("Invalid user")

        // create imei
        DeviceDAO().post(mac = mac)
    }
}
