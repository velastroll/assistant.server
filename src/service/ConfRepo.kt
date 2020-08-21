package com.percomp.assistant.core.services

import com.percomp.assistant.core.controller.services.ConfService
import com.percomp.assistant.core.model.ConfBody
import com.percomp.assistant.core.model.ConfData
import com.percomp.assistant.core.model.Confs
import com.percomp.assistant.core.services.DatabaseFactory.dbQuery
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*

class ConfRepo : ConfService {

    /**
     * This method retrieves the current configuration data of a specific filter. Filter could be either a device identifier as a
     * postal code identifier, or "GLOBAL" to get the global configuration.
     *
     * @param filter could be either a device identifier as a postal code identifier, or "GLOBAL" to get the global configuration.
     * @param pending is the status of the configuration for individual device configuration.
     * @return the current filtered configuration.
     */
    override fun get(device : String, pending : Boolean) : ConfData? {
        return runBlocking {
            return@runBlocking dbQuery {
                return@dbQuery Confs
                    .select({ Confs.receiver eq device and (Confs.pending eq pending) })
                    .orderBy(Confs.timestamp, isAsc = false)
                    .map {
                        ConfData(
                            device = it[Confs.receiver],
                            body = ConfBody(sleep_sec = it[Confs.sleep_sec]),
                            timestamp = it[Confs.timestamp],
                            pending = it[Confs.pending]
                        )
                    }.firstOrNull()
            }
        }
    }

    /**
     * This method saves that a specific device has been updated with the configuration which the datetime specified.
     * @param mac is the identifier of the device which has been updated.
     * @param datatime is the same datetime than the datetime of the configuration used to update the device.
     */
    override fun done(mac: String, timestamp: String) {
        runBlocking {
            dbQuery {
                Confs.update({ Confs.receiver eq mac and (Confs.timestamp eq timestamp) }) {
                    it[Confs.pending] = false
                }
            }
        }
    }

    /**
     * This method creates a new configuration.
     * @param data is the data configuration.
     */
    override fun post(data : ConfData) {
        runBlocking {
            dbQuery {
                Confs.insert {
                    it[Confs.receiver] = data.device!!
                    it[Confs.sleep_sec] = data.body!!.sleep_sec
                    it[Confs.timestamp] = data.timestamp
                    it[Confs.pending] = data.pending
                }
            }
        }
    }

    /**
     * This method deletes a specific configuration.
     * @param id is the device identifier of the configuration.
     * @param datetime is the datetime of the configuration.
     */
    override fun delete(id : String, datetime: String) {
        runBlocking {
            dbQuery {
                Confs.deleteWhere{
                    Confs.timestamp eq datetime and (Confs.receiver eq id)
                }
            }
        }
    }

    /**
     * This method deletes a specific configuration.
     * @param id is the device identifier of the configuration.
     * @param pending is the state of the configuration.
     */
    override fun delete(id : String, pending: Boolean) {
        runBlocking {
            dbQuery {
                Confs.deleteWhere { Confs.pending eq pending and (Confs.receiver eq id) }
            }
        }
    }
}
