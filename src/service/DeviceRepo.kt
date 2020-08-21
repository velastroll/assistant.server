package com.percomp.assistant.core.services

import com.percomp.assistant.core.controller.services.DeviceService
import com.percomp.assistant.core.model.*
import com.percomp.assistant.core.services.DatabaseFactory.dbQuery
import com.percomp.assistant.core.util.Constants
import com.percomp.assistant.core.util.Constants.HEX
import com.percomp.assistant.core.util.communication.RaspiAction
import io.ktor.auth.OAuth2Exception
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import java.time.Instant

class DeviceRepo : DeviceService {

    /**
     * Return the content of the last task, configured to a specific user.
     *
     * @param device is the owner of the wifi data.
     * @param task is the task of the data.
     */
    override fun getContent(device: String, task: String): String? {
        return runBlocking {
            return@runBlocking dbQuery {
                Tasks
                    .select({Tasks.device eq device and (Tasks.event eq task)}).orderBy(Tasks.timestamp, isAsc = false)
                    .map {
                            it[Tasks.content]
                        }
                    .firstOrNull()
            }
        }
    }

    /**
     * This method checks if the combination of mac device and password is correct.
     * @param mac is the device identifier.
     * @param password is the secret device key.
     */
    override fun check(mac: String, password: String): Boolean {
        return  runBlocking {
            return@runBlocking dbQuery {

                if (mac.isEmpty()) throw IllegalArgumentException("Checking user: the username is too short.")
                if (password.isEmpty()) throw IllegalArgumentException("Checking user: the password is too short.")

                // If the combination is right return true and create a new device if does not exist yet.
                if (decode(password).equals(mac)) {
                    runBlocking {
                        if (checkExists(mac) == null) {
                            post(mac = mac)
                        }
                    }
                    // TODO: update status on DeviceCtrl instead of here
                    // update their status
                    newStatus(mac, RaspiAction.LOGIN)
                    return@dbQuery true
                } else {
                    return@dbQuery false
                }
            }
        }
    }

    /**
     * This method retrieves device info if the device exists.
     * @param mac is the device identifier.
     * @return Device information or null.
     */
    override fun checkExists(mac: String): Device? {
        return runBlocking {
            return@runBlocking dbQuery {

                return@dbQuery Devices.select { Devices.id eq mac }.map {
                    Device(
                        mac = mac
                    )
                }.firstOrNull()
            }
        }
    }

    /**
     * Creates a new device on the system, and assign it a password.
     * @param mac is the device identifier.
     */
    override fun post(mac : String)  {
        runBlocking {
            dbQuery {
                Devices.insert {
                    it[Devices.id] = mac
                }
            }
        }
    }

    /**
     * This method retrieves all the devices
     * @return a list of devices.
     */
    override fun getAll(): List<Device> {
        return runBlocking {
            return@runBlocking dbQuery {
                return@dbQuery Devices.selectAll().map {
                    Device(mac = it[Devices.id])
                }
            }
        }
    }

    /**
     * This method assigns a specific user to a specific device.
     * @param nif national identification of user who is gonna be assigned to the device.
     * @param mac is the identifier of the device.
     */
    override fun assignUser(nif: String, mac: String) {
        val id = (1..Constants.SALT)
            .map { kotlin.random.Random.nextInt(0, Constants.CHARPOOL.size) }
            .map(Constants.CHARPOOL::get)
            .joinToString("")
        runBlocking {
            dbQuery {
                Relations.insert {
                    it[Relations.device] = mac
                    it[Relations.from] = Instant.now().toString()
                    it[Relations.user] = nif
                    it[Relations.id] = id
                }
            }
        }
    }

    /**
     * This method retrieves the last assignment of a specific device.
     * @param mac is the device identifier.
     * @return last assignment or null.
     */
    override fun getLastAssignment(mac: String): Relation? {
        return runBlocking {
            return@runBlocking dbQuery {
                // retrieve a specific relation by mac device.
                val r = Relations.select({Relations.device eq mac and (Relations.to.isNull())})
                    .orderBy(Relations.from, isAsc = false)
                    .map {

                        // retrieve user
                        val user = People
                            .select({People.nie eq it[Relations.user]})
                            .map {
                                Person(
                                    name = it[People.name],
                                    surname = it[People.surname],
                                    nif = it[People.nie],
                                    postcode = it[People.location]) }
                            .first()

                        // return relation
                        return@map Relation(
                            user = user,
                            from = it[Relations.from],
                            to = it[Relations.to]
                        )
                    }.firstOrNull()

                // retrieve position
                if (r != null) {
                    r.position = Locations.select({ Locations.postcode eq r.user!!.postcode }).map {
                        Position(
                            lat = it[Locations.lat],
                            lon = it[Locations.lon]
                        )
                    }.firstOrNull()
                }
                return@dbQuery r
            }
        }
    }

    /**
     * This method ends with the relation of a person with a device.
     * @param mac is the identifier of the device which is gonna be without assignment with people.
     */
    override fun endAssignment(mac: String) {
        runBlocking {
            dbQuery {
                Relations.update (
                    {
                        Relations.device eq mac and (Relations.to.isNull())
                    }){
                    it[Relations.to] = Instant.now().toString()
                }
            }
        }
    }

    /**
     * This method retrieves the current assignment of a person if exists.
     * @param nif is the national identifier of the person.
     * @return last assignment or null.
     */
    override fun getCurrentAssignmentWithUser(nif: String): Relation? {
        return runBlocking {
            return@runBlocking dbQuery {
                return@dbQuery Relations.select({Relations.user eq nif and (Relations.to.isNull())})
                    .map {
                        Relation(
                            device = it[Relations.device],
                            from = it[Relations.from]
                        )
                    }.singleOrNull()
            }
        }
    }

    /**
     * This method saves a new device status.
     * @param mac is the device identifier.
     * @param status is the device action.
     * @param content is a description of the task.
     */
    override fun newStatus(mac: String, status: RaspiAction, content: String?) {
        runBlocking {
            dbQuery {
                // generate id
                val id = (1..Constants.SALT)
                    .map { kotlin.random.Random.nextInt(0, Constants.HEX.size) }
                    .map(Constants.HEX::get)
                    .joinToString("")

                Status.insert {
                    it[Status.id] = id
                    it[Status.device] = mac
                    it[Status.timestamp] = Instant.now().toString()
                    it[Status.status] = status
                    it[Status.content] = content
                }
            }
        }
    }

    /**
     * This method retrieves the last five status of a specific device.
     * @param mac is the device identifier.
     */
    override fun getLastFiveStatus(mac: String): List<StateBasic> {
        return runBlocking {
            return@runBlocking dbQuery {
                return@dbQuery Status.select ({ Status.device eq mac }).orderBy( Status.timestamp, isAsc = false ).limit(5).map {
                    StateBasic(
                        type = it[Status.content] ?: it[Status.status].toString(),
                        timestamp = it[Status.timestamp]
                    )
                }
            }
        }
    }

    /** ---------- */

    /**
     * TODO: put this method on DecrypterService
     */
    private fun decode(password: String) : String {
        if (password.length != 19) throw OAuth2Exception.InvalidGrant("Invalid credentials.")

        var new = ""
        for (c in password){
            if (HEX.contains(c)) new = "$new$c"
        }
        val s =  new.substring(8, 10) + ":" + new.substring(4,6) + ":" + new.substring(10,12) + ":"+ new.substring(0, 2) + ":" + new.substring(6, 8) + ":" + new.substring(2,4)
        return s
    }
}