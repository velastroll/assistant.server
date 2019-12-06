package com.percomp.assistant.core.dao

import com.percomp.assistant.core.dao.DatabaseFactory.dbQuery
import com.percomp.assistant.core.domain.Locations
import com.percomp.assistant.core.domain.People
import com.percomp.assistant.core.domain.Relation
import com.percomp.assistant.core.model.Person
import com.percomp.assistant.core.model.Position
import com.percomp.assistant.core.util.Constants
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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
            it[Relation.user] = nifUC
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
        val r = Relation.select({Relation.device eq mac and (Relation.to.isNull())})
            .orderBy(Relation.from, isAsc = false)
            .map {

                // retrieve user
                val user = People
                    .select({People.nie eq it[Relation.user]})
                    .map {
                        Person(
                            name = it[People.name],
                            surname = it[People.surname],
                            nif = it[People.nie],
                            postcode = it[People.location]) }
                    .first()

                // return relation
                com.percomp.assistant.core.model.Relation(
                    user = user,
                    from = it[Relation.from],
                    to = it[Relation.to]
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


    suspend fun getCurrentByUser(user: String) : com.percomp.assistant.core.model.Relation? = dbQuery {
        Relation.select({Relation.user eq user})
            .map {
                com.percomp.assistant.core.model.Relation(
                    device = it[Relation.device],
                    from = it[Relation.from]
                    )
            }.singleOrNull()
    }

}