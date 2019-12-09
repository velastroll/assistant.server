package com.percomp.assistant.core.controller.services

import com.percomp.assistant.core.dao.*
import com.percomp.assistant.core.model.*
import io.ktor.auth.OAuth2Exception
import java.time.Instant

class ConfCtrl {

    suspend fun get(mac : String?, pending: Boolean = true) : ConfData? {

        // check if user exist on db
        if (mac.isNullOrEmpty()) throw OAuth2Exception.InvalidGrant("Not valid device")

        // check it on db
        if (DeviceDAO().checkExists(mac) == null) throw IllegalStateException("Device does not exist.")

        // retrieve pending or actual config
        val conf = ConfDAO().get(mac, pending = true) ?: ConfDAO().get(mac, pending = false)

        // return it
        return conf
    }

    suspend fun complete(mac: String?, timestamp : String?) {
        // check if user exist on db
        if (mac.isNullOrEmpty()) throw OAuth2Exception.InvalidGrant("Not valid device")
        if (timestamp.isNullOrEmpty()) throw OAuth2Exception.InvalidGrant("Not valid device")

        // check it on db
        DeviceDAO().checkExists(mac) ?: throw IllegalStateException("Device does not exist.")

        // mark conf as done
        ConfDAO().done(mac, timestamp)
    }

    suspend fun create(configuration: ConfData) {

        // checks values
        if (configuration.device.isNullOrEmpty()) throw IllegalArgumentException("Device is not specified.")
        if (DeviceDAO().checkExists(configuration.device!!) == null) throw IllegalArgumentException("Device ${configuration.device} does not exist.")
        if (configuration.body == null) throw IllegalArgumentException("Configuration has not body to configure.")
        if (configuration.body!!.sleep_sec < 10) throw IllegalArgumentException("Sleep time ${configuration.body!!.sleep_sec} should be greater or equal than 10 seconds.")

        // configures parameters
        configuration.pending = true
        configuration.timestamp = Instant.now().toString()

        // creates config
        ConfDAO().post(data = configuration)
    }
}
