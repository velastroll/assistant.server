package com.percomp.assistant.core.controller.domain

import com.percomp.assistant.core.app.config.oauth.Token
import com.percomp.assistant.core.app.config.oauth.TokenCtrl
import com.percomp.assistant.core.controller.services.LocationService
import com.percomp.assistant.core.model.*
import com.percomp.assistant.core.rest.CredentialRequest
import com.percomp.assistant.core.rest.IntervalOfDates
import controller.services.DeviceService
import controller.services.IntentsService
import controller.services.PeopleService
import controller.services.TaskService
import io.ktor.auth.OAuth2Exception
import model.Intent
import model.IntentDone
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.time.Instant

class DeviceCtrl : KoinComponent {

    val deviceService : DeviceService by inject()
    val taskService: TaskService by inject()
    val peopleService: PeopleService by inject()
    val locationService: LocationService by inject()
    private val authService : TokenCtrl by inject()
    private val intentsService : IntentsService by inject()


    fun check(auth : CredentialRequest) : Token {

        // check if user exist on db
        if (auth.user.isNullOrEmpty()) throw OAuth2Exception.InvalidGrant("Not valid user")
        if (auth.password.isNullOrEmpty()) throw OAuth2Exception.InvalidGrant("Not valid password")
        // check it on db

        if (!deviceService.check(auth.user, auth.password)) throw OAuth2Exception.InvalidGrant("Not valid user or password.")
        // store tokens
        val tokens = authService.newTokens(username = auth.user)
        // return it
        return tokens
    }

    fun exist(mac: String) : Device? {
        if (mac.isNullOrEmpty()) return null
        val device = deviceService.checkExists(mac) ?: return null
        return device
    }

    fun create(mac : String){
        // check if user exist on db
        if (mac.isNullOrEmpty()) throw IllegalArgumentException("Invalid user")
        if (mac.length != 17 ) throw IllegalArgumentException("Invalid user")
        if (!mac.contains(":")) throw IllegalArgumentException("Invalid user")

        // create imei
        deviceService.post(mac = mac)
    }

    fun getAll(): List<Device4W> {
        // retrieve all
        val devices = deviceService.getAll()
        val devices4w = ArrayList<Device4W>()
        for ( d in devices ) {
            val d4 = Device4W(device = d.mac)
            // retrieve the last status
            d4.last_status = deviceService.getLastFiveStatus(mac=d.mac)
            // retrieve last events
            d4.last_events = taskService.getLastFiveTasks(mac = d.mac)
            // retrieve last intents

            // retrieve relation
            d4.relation = deviceService.getLastAssignment(mac = d.mac)
            // retrieve pending actions
            d4.pending = taskService.getPendingTaskForDevice(d.mac)
            // retrieve position

            //add to list
            devices4w.add(d4)

        }
        return devices4w
    }

    fun addRelation(nif: String, device: String) {
        // check if person exists
        val person = peopleService.getPerson(nif) ?: throw IllegalArgumentException("no person with nif = $nif.")

        // check if device exists
        val device = deviceService.checkExists(mac = device) ?: throw IllegalArgumentException("no device with mac = $device.")

        // add relation
        deviceService.assignUser(nif = person.nif, mac = device.mac)
    }

    fun finishRelation(mac: String) {
        // check if exist
        val device = deviceService.checkExists(mac = mac) ?: throw IllegalArgumentException("no device with mac = $mac.")

        // delete it
        deviceService.endAssignment(mac = device.mac)

    }

    fun getRelation(mac: String) : Relation?{
        val r = deviceService.getLastAssignment(mac)
        if (r!=null) {
            val l = locationService.getLocationByPostalCode(postalCode = r.user!!.postcode)
                ?: throw IllegalArgumentException("Does not exist a location with postal code ${r.user!!.postcode}")
            r.info = l.name
        }
        return r
    }

    /**
     * This method checks the intent done, and prepares it to be saved.
     */
    fun newIntentAction(device: String?, intent: IntentDone) {
        // parse intent values
        val toStore = Intent(
            datetime = intent.datetime ?: Instant.now().toString(),
            data = intent.intent,
            slots = intent.slots,
            device = device ?: throw IllegalArgumentException("Device has not been specified.")
        )

        intentsService.addIntentAction(data = toStore)
    }

    fun retrieveIntents(device: String, interval: IntervalOfDates): List<Intent> {
        val d =
    }
}
