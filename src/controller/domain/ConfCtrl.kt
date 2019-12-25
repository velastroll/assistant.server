package com.percomp.assistant.core.controller.domain

import com.percomp.assistant.core.controller.services.Location
import com.percomp.assistant.core.controller.services.LocationService
import com.percomp.assistant.core.model.*
import controller.services.ConfService
import controller.services.DeviceService
import io.ktor.auth.OAuth2Exception
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.time.Instant

class ConfCtrl: KoinComponent{


    private val deviceService: DeviceService by inject()
    private val confService : ConfService by inject()
    private val locationService: LocationService by inject()


    /**
     * Retrieve the pending or actual configuration of the device
     */
    fun get(mac : String?, pending: Boolean = true) : ConfData? {

        // check if user exist on db
        if (mac.isNullOrEmpty()) throw OAuth2Exception.InvalidGrant("Not valid device")

        // check it on db
        if (deviceService.checkExists(mac) == null) throw IllegalStateException("Device does not exist.")

        // retrieve pending or actual app.config
        val conf = confService.get(mac, pending = true) ?: confService.get(mac, pending = false)

        // return it
        return conf
    }

    /**
     * Sets a specific configuration as done.
     */
    fun complete(mac: String?, timestamp : String?) {
        // check if user exist on db
        if (mac.isNullOrEmpty()) throw OAuth2Exception.InvalidGrant("Not valid device")
        if (timestamp.isNullOrEmpty()) throw OAuth2Exception.InvalidGrant("Not valid timestamp")

        // check it on db
        deviceService.checkExists(mac) ?: throw IllegalStateException("Device does not exist.")

        // mark conf as done
        confService.done(mac, timestamp)
    }

    /**
     * Creates a new configuration.
     */
     fun create(configuration: ConfData) {

        // checks values
        if (configuration.device.isNullOrEmpty()) throw IllegalArgumentException("Device is not specified.")
        if (deviceService.checkExists(configuration.device!!) == null) throw IllegalArgumentException("Device ${configuration.device} does not exist.")
        if (configuration.body == null) throw IllegalArgumentException("Configuration has not body to configure.")
        if (configuration.body!!.sleep_sec < 10) throw IllegalArgumentException("Sleep time ${configuration.body!!.sleep_sec} should be greater or equal than 10 seconds.")

        // configures parameters
        configuration.pending = true
        configuration.timestamp = Instant.now().toString()

        // creates app.config
        confService.post(data = configuration)
    }


    /* ==== new version ==== */

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
                var pc = 0
                try {
                    pc = data.device!!.toInt()
                } catch (e : Exception) {
                    throw IllegalArgumentException("Not valid postcode ${data.device}")
                }
                val location : Location = locationService.getLocationByPostalCode(postalCode = pc)
                    ?: throw IllegalArgumentException("Does not exist location with postal code $pc")
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

data class ConfDatas (
    var global : ConfData? = null,
    var location : ConfData? = null,
    var deviceConf : ConfData? = null,
    var pendingConf : ConfData? = null
)
