package com.percomp.assistant.core.controller.services

import com.percomp.assistant.core.config.Token
import com.percomp.assistant.core.config.newTokens
import com.percomp.assistant.core.dao.*
import com.percomp.assistant.core.model.*
import com.percomp.assistant.core.services.CredentialRequest
import com.percomp.assistant.core.util.communication.RaspiAction
import io.ktor.auth.OAuth2Exception

class DeviceCtrl {

    suspend fun check(auth : CredentialRequest) : Token {

        // check if user exist on db
        if (auth.user.isNullOrEmpty()) throw OAuth2Exception.InvalidGrant("Not valid user")
        if (auth.password.isNullOrEmpty()) throw OAuth2Exception.InvalidGrant("Not valid password")
        // check it on db

        if (!DeviceDAO().check(auth.user, auth.password)) throw OAuth2Exception.InvalidGrant("Not valid user or password.")
        // store tokens
        val tokens = newTokens(username = auth.user)
        // return it
        return tokens
    }

    suspend fun exist(mac: String) : Device? {
        if (mac.isNullOrEmpty()) return null
        val device = DeviceDAO().checkExists(mac) ?: return null
        return device
    }

    suspend fun create(mac : String){
        // check if user exist on db
        if (mac.isNullOrEmpty()) throw IllegalArgumentException("Invalid user")
        if (mac.length != 17 ) throw IllegalArgumentException("Invalid user")
        if (!mac.contains(":")) throw IllegalArgumentException("Invalid user")

        // create imei
        DeviceDAO().post(mac = mac)
    }

    suspend fun getAll(): List<Device4W> {
        // retrieve all
        val devices = DeviceDAO().getAll()
        val devices4w = ArrayList<Device4W>()
        for ( d in devices ) {
            val d4 = Device4W(device = d.mac)
            // retrieve the last status
            d4.last_status = StatusDAO().getLastFive(mac=d.mac)
            // retrieve last events
            d4.last_events = StatusDAO().getLastFiveTasks(mac = d.mac)
            // retrieve last intents

            // retrieve relation
            d4.relation = RelationDAO().get(mac = d.mac)
            // retrieve pending actions
            d4.pending = TaskDAO().getPending(d.mac)

            //add to list
            devices4w.add(d4)

        }
        return devices4w
    }

    suspend fun addRelation(nif: String, device: String) {
        // check if person exists
        val person = PeopleDAO().get(nif) ?: throw IllegalArgumentException("no person with nif = $nif.")

        // check if device exists
        val device = DeviceDAO().checkExists(mac = device) ?: throw IllegalArgumentException("no device with mac = $device.")

        // add relation
        RelationDAO().post(nif = person.nif, device = device.mac)
    }

    suspend fun finishRelation(mac: String) {
        // check if exist
        val device = DeviceDAO().checkExists(mac = mac) ?: throw IllegalArgumentException("no device with mac = $mac.")

        // delete it
        RelationDAO().finish(mac = device.mac)

    }

}
