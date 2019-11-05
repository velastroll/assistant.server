package com.percomp.assistant.core.controller.services

import com.percomp.assistant.core.config.Token
import com.percomp.assistant.core.config.newTokens
import com.percomp.assistant.core.dao.DeviceDAO
import com.percomp.assistant.core.dao.PeopleDAO
import com.percomp.assistant.core.dao.RelationDAO
import com.percomp.assistant.core.dao.StatusDAO
import com.percomp.assistant.core.model.Device
import com.percomp.assistant.core.model.Relation
import com.percomp.assistant.core.model.State
import com.percomp.assistant.core.model.UserType
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

    suspend fun exist(mac: String) : UserType? {
        if (mac.isNullOrEmpty()) return null
        if (DeviceDAO().checkExists(mac) == null) return null
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

    suspend fun getAll(): List<Device> {
        // retrieve all
        val devices = DeviceDAO().getAll()
        val status = ArrayList<State?>()
        // retrieve the last status
        for ( d in devices ) {
            //retrieve status
            val state = StatusDAO().get(mac=d.mac)
            status.add(state)
            // retrieve relation
            val relation = RelationDAO().get(mac = d.mac)
            d.relation = relation
            d.status = status
        }
        return devices
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
