package com.percomp.assistant.core.dao

import com.percomp.assistant.core.dao.DatabaseFactory.dbQuery
import com.percomp.assistant.core.domain.Devices
import com.percomp.assistant.core.domain.People
import com.percomp.assistant.core.domain.Relation
import com.percomp.assistant.core.model.Person
import com.percomp.assistant.core.util.Constants
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.joda.time.Instant

class RelationDAO {


    /**
     * Create a new user in the database.
     * @param signUp is the form to sign up in the system.
     */
    suspend fun post(nif: String, device: String) = dbQuery {

        // crypt password
        val nifUC = nif.toUpperCase()
        val id = (1..Constants.SALT)
            .map { kotlin.random.Random.nextInt(0, Constants.CHARPOOL.size) }
            .map(Constants.CHARPOOL::get)
            .joinToString("")
        // insert
        Relation.insert {
            it[Relation.person] = nifUC
            it[Relation.device] = device
            it[Relation.id] = id
            it[Relation.from] = Instant.now().toString()
        }
    }


    /**
     * Retrieve the last relation of a specific device.
     * @param mac identifier of the device
     */
    suspend fun get(mac : String) = dbQuery {
        // retrieve a specific relation by mac device.
        Relation.select({Relation.device eq mac})
            .orderBy(Relation.from, isAsc = false)
            .map {

                // retrieve user
                val user = People
                    .select({People.nie eq it[Relation.person]})
                    .map {
                        Person(
                            name = it[People.name],
                            nif = it[People.nie]) }
                    .first()

                // return relation
                com.percomp.assistant.core.model.Relation(
                    user = user,
                    from = it[Relation.from],
                    to = it[Relation.to]
                )
            }.firstOrNull()
    }

    /**
     * End the active relation of a specified device.
     * @param mac device identifier
     */
    suspend fun finish(mac: String) = dbQuery{
        Relation.update (
            {
                Relation.device eq mac and (Relation.to.isNull())
            }){
                it[Relation.to] = Instant.now().toString()
        }
    }

}