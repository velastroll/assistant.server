package com.percomp.assistant.core.dao

import com.percomp.assistant.core.controller.services.Location
import com.percomp.assistant.core.controller.services.Province
import com.percomp.assistant.core.dao.DatabaseFactory.dbQuery
import com.percomp.assistant.core.domain.Locations
import com.percomp.assistant.core.domain.Provinces
import com.percomp.assistant.core.model.Position
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class LocationRepo {

    /**
     * Insert a new town in the database
     */
    suspend fun post (name: String, postCode : Int, province : Int, lat : Double, lon: Double) = dbQuery{
        Locations.insert {
            it[Locations.name] = name
            it[Locations.postcode] = postCode
            it[Locations.lat] = lat
            it[Locations.lon] = lon
            it[Locations.province] = province
        }
    }

    /**
     * Retrieve all the provinces
     */
    suspend fun getProvinces(): List<Province> = dbQuery {
        return@dbQuery Provinces.selectAll().map{
            Province(
                code = it[Provinces.code],
                name = it[Provinces.name],
                locations = ArrayList()
            )
        } as ArrayList
    }

    /**
     * Retrieve all the locations of a specific province
     */
    suspend fun get(code: Int): ArrayList<Location> = dbQuery {
        return@dbQuery Locations.select({Locations.province eq code}).map {
            com.percomp.assistant.core.controller.services.Location(
                name = it[Locations.name],
                postcode = it[Locations.postcode],
                latitude = it[Locations.lat],
                longitude = it[Locations.lon],
                people = ArrayList()
            )
        } as ArrayList<Location>
    }

    /**
     * Retrieves a specific location
     */
    suspend fun getByPostalCode(postcode: Int): Location? = dbQuery {
        return@dbQuery Locations.select({Locations.postcode eq postcode}).map {
            com.percomp.assistant.core.controller.services.Location(
                name = it[Locations.name],
                postcode = it[Locations.postcode],
                latitude = it[Locations.lat],
                longitude = it[Locations.lon],
                people = ArrayList()
            )
        }.firstOrNull()
    }

}