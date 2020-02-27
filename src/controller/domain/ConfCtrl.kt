package com.percomp.assistant.core.controller.domain

import com.percomp.assistant.core.controller.services.Location
import com.percomp.assistant.core.controller.services.LocationService
import com.percomp.assistant.core.model.*
import controller.services.ConfService
import controller.services.DeviceService
import io.ktor.auth.OAuth2Exception
import io.ktor.util.KtorExperimentalAPI
import java.time.Instant

@KtorExperimentalAPI
class ConfCtrl(
    private val deviceService: DeviceService,
    private val confService : ConfService,
    private val locationService: LocationService){


    /**
     * Returns a list for specific device with:
     *  - Global configuration.
     *  - Location configuration.
     *  - Actual device configuration
     *  - Pending device configuration
     */
    fun getConfs(mac: String) : ConfDatas {

        val confs = ConfDatas()

        // retrieves global configuration
        confs.global = confService.get("GLOBAL", pending = false)
        // retrieves it location data configuration
        val relation : Relation? = deviceService.getLastAssignment(mac)
        if (relation != null) confs.location = confService.get(relation.user!!.postcode.toString(), pending = false)

        // retrieves device conf
        confs.deviceConf = confService.get(mac, false)

        // retrieve pending conf
        confs.pendingConf = confService.get(mac, true)

        return confs
    }

    /**
     * Returns the current configuration.
     */
    fun getActual(mac: String) : ConfData{
        val conf = getConfs(mac = mac)
        val actual : ArrayList<ConfData> = ArrayList()
        if (conf.global != null) actual.add(conf.global!!)
        if (conf.location != null) actual.add(conf.location!!)
        if (conf.pendingConf != null) actual.add(conf.pendingConf!!)
        if (conf.deviceConf != null) actual.add(conf.deviceConf!!)

        var new = ConfData(device = mac, timestamp = "0000")
        for (c in actual){
            if (c.timestamp > new.timestamp) new = c
        }

        // return the currently configuration
        if (new.timestamp != "0000"){
            new.pending = true
            return new
        } else throw IllegalArgumentException("No available configuration")
    }

    /**
     * Creates a new configuration.
     * It could be either GLOBAL as for a postal code, or for a specific device.
     */
    fun new(data: ConfData){
        if (data.device.isNullOrEmpty())
            throw IllegalArgumentException("Device is not specified.")
        if (data.body == null)
            throw IllegalArgumentException("Configuration has not body to configure.")
        if (data.body!!.sleep_sec < 10)
            throw IllegalArgumentException("Sleep time ${data.body!!.sleep_sec} should be greater or equal than 10 seconds.")

        data.timestamp = Instant.now().toString()
        when{
            data.device == "GLOBAL" -> {
                // delete old global∫
                confService.delete("GLOBAL", pending = false)
                // Configure irrelevant values
                data.pending = false
                // create new global conf
                confService.post(data)
            }
            data.device!!.length < 6 -> {
                // check if it0s a valid postcode
                val pc : Int
                try {
                    pc = data.device!!.toInt()
                } catch (e : Exception) {
                    throw IllegalArgumentException("Not valid postcode ${data.device}")
                }
                if (locationService.getLocationByPostalCode(postalCode = pc) == null)
                    throw IllegalArgumentException("Does not exist location with postal code $pc")
                // create new global conf
                data.device = "$pc"
                data.pending = false
                confService.post(data)
            }
            data.device!!.length > 16 -> {
                // device identifier
                if (deviceService.checkExists(data.device!!) == null) throw IllegalArgumentException("Device ${data.device} does not exist.")
                // delete pending
                confService.delete(data.device!!, pending = true)
                // creates a new pending
                data.pending = true
                confService.post(data)
            }

        }

    }

    /**
     * Device has been updated, so this updates the current configuration.
     */
    fun updated(timestamp: String?, mac: String?){

        // checks values
        if (mac.isNullOrEmpty()) throw OAuth2Exception.InvalidGrant("Not valid device: $mac")
        if (timestamp.isNullOrEmpty()) throw OAuth2Exception.InvalidGrant("Not valid timestamp: $timestamp")
        deviceService.checkExists(mac) ?: throw IllegalStateException("Device $mac does not exist.")

        // retrieve data
        val confs = getConfs(mac)
        val actual = ConfData(device = mac, timestamp = timestamp, pending = false)

        // configure database
        if (confs.global != null && confs.global!!.timestamp == timestamp){
            // delete all device conf
            try {
                confService.delete(mac, true)
                confService.delete(mac, false)
            }catch (e:Exception){println(1)}
            // insert
            actual.body = confs.global!!.body
            // insert new configuration
            confService.post(actual)
        } else if (confs.location != null && confs.location!!.timestamp == timestamp){
            // delete all device conf
            try{
                confService.delete(mac, true)
                confService.delete(mac, false)
            }catch (e:Exception){println(2)}
            // insert
            actual.body = confs.location!!.body
            // insert new configuration
            confService.post(actual)
        } else if (confs.pendingConf != null && confs.pendingConf!!.timestamp == timestamp){
            // delete all device conf
            try{
                confService.delete(mac, false)
                confService.done(mac, timestamp)
            }catch (e:Exception){println(3)}
        } else {
            throw IllegalArgumentException("Invalid timestamp $timestamp for device $mac")
        }
    }
}

