package com.percomp.assistant.core.dao

import com.percomp.assistant.core.dao.DatabaseFactory.dbQuery
import com.percomp.assistant.core.domain.Confs
import com.percomp.assistant.core.domain.Events
import com.percomp.assistant.core.domain.Tasks
import com.percomp.assistant.core.model.ConfBody
import com.percomp.assistant.core.model.ConfData
import com.percomp.assistant.core.model.Event
import com.percomp.assistant.core.model.Task
import com.percomp.assistant.core.util.Constants
import org.jetbrains.exposed.sql.*
import java.security.MessageDigest

class ConfDAO {

    /**
     * Retrieve the device config
     * @param device mac direction
     * @return [ConfData] or null
     */
    suspend fun get(device : String, pending : Boolean = true) : ConfData? = dbQuery{
        return@dbQuery Confs
            .select({Confs.receiver eq device and (Confs.pending eq pending)})
            .orderBy(Confs.timestamp, isAsc=false)
            .map {
                ConfData(
                    device = it[Confs.receiver],
                    body = ConfBody( sleep_sec = it[Confs.sleep_sec]),
                    timestamp=it[Confs.timestamp],
                    pending = it[Confs.pending]
                    )
            }.firstOrNull()
    }

    /**
     * Marks a pending configuration as a done.
     * @param mac device identifier
     * @param timestamp configuration unique timestamp
     */
    suspend fun done(mac: String, timestamp: String) = dbQuery{
        Confs.update ({ Confs.receiver eq mac and (Confs.timestamp eq timestamp) }){
            it[Confs.pending] = false
        }
    }

    /**
     * Creates a new pending configuration for a specific device.
     * @param mac device identifier
     * @param data new configuration
     */
    suspend fun post(data : ConfData) = dbQuery{
        Confs.insert {
            it[Confs.receiver] = data.device!!
            it[Confs.sleep_sec] = data.body!!.sleep_sec
            it[Confs.timestamp] = data.timestamp
            it[Confs.pending] = data.pending
        }
    }

    /**
     * Deletes a specific configuration
     * @param device identifier of configuration to delete. Could be a postcode, a device identifier, o 'GLOBAL'.
     * @param timestamp date identifier of the configuration to delete
     */
    suspend fun delete(id : String, timestamp: String) = dbQuery {
        Confs.deleteWhere{Confs.timestamp eq timestamp and (Confs.receiver eq id)}
    }

    /**
     * Deletes a specific configuration
     * @param device identifier of configuration to delete. Could be a postcode, a device identifier, o 'GLOBAL'.
     * @param timestamp date identifier of the configuration to delete
     */
    suspend fun delete(id : String, pending: Boolean) = dbQuery {
        Confs.deleteWhere{Confs.pending eq pending and (Confs.receiver eq id)}
    }
}
