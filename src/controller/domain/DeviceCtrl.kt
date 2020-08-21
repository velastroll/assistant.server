package com.percomp.assistant.core.controller.domain

import com.percomp.assistant.core.controller.services.*
import com.percomp.assistant.core.model.*
import com.percomp.assistant.core.util.Constants
import io.ktor.auth.OAuth2Exception
import java.time.Instant

class DeviceCtrl(
    private val deviceService : DeviceService,
    private val taskService: TaskService,
    private val peopleService: PeopleService,
    private val locationService: LocationService,
    private val authService : AuthService,
    private val intentsService : IntentsService
){

    fun check(auth : CredentialRequest) : Token {

        // check if user exist on db
        if (auth.user.isEmpty()) throw OAuth2Exception.InvalidGrant("Not valid user")
        if (auth.password.isEmpty()) throw OAuth2Exception.InvalidGrant("Not valid password")
        // check it on db

        if (!deviceService.check(auth.user, auth.password)) throw OAuth2Exception.InvalidGrant("Not valid user or password.")
        // store tokens
        // return it
        return authService.newTokens(username = auth.user)
    }

    fun exist(mac: String) : Device? {
        if (mac.isEmpty()) return null
        return deviceService.checkExists(mac) ?: return null
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
            d4.last_intent = intentsService.getLastIntent(mac = d.mac)
            // retrieve relation
            d4.relation = deviceService.getLastAssignment(mac = d.mac)
            // retrieve pending actions
            d4.pending = taskService.getPendingTaskForDevice(d.mac)
            //add to list
            devices4w.add(d4)

        }
        return devices4w
    }

    fun addRelation(nif: String, device: String) {
        // check if person exists
        val person = peopleService.getPerson(nif) ?: throw IllegalArgumentException("no person with nif = $nif.")

        // check if device exists
        val d = deviceService.checkExists(mac = device) ?: throw IllegalArgumentException("no device with mac = $device.")

        // add relation
        deviceService.assignUser(nif = person.nif, mac = d.mac)
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

    /**
     * This method checks if device exists and checks the interval of dates to retrieve the list of intents.
     * @param device the device mac identifier
     *
     */
    fun retrieveIntents(device: String, interval: IntervalOfDates): List<Intent> {
        // checks device
        var d: Device? = null
        deviceService.getAll().forEach { if (it.mac == device) d = it }
        if (d == null) throw IllegalArgumentException("Device $device does not exist.")
        var f = interval.from ?: Constants.DATE_PAST
        var t = interval.to ?: Constants.DATE_FUTURE
        if (Instant.parse(f).isAfter(Instant.parse(t))) throw IllegalArgumentException("FROM [$f] is after TO [$t]")
        // check if the device has any relation
        val u = deviceService.getLastAssignment(device)
        if (u != null){
            // the device has an assignment, check min and max dates of interval
            if (Instant.parse(u.from).isAfter(Instant.parse(f))) f = u.from
            if (Instant.parse(u.from).isAfter(Instant.parse(t))) t = u.from
        }
        // returns it
        return intentsService.getIntents(d!!.mac, f, t)
    }
}
