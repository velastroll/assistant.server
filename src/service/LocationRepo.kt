package com.percomp.assistant.core.services

import com.percomp.assistant.core.controller.services.Location
import com.percomp.assistant.core.controller.services.LocationService
import com.percomp.assistant.core.controller.services.Province
import com.percomp.assistant.core.model.Locations
import com.percomp.assistant.core.model.Position
import com.percomp.assistant.core.model.Provinces
import com.percomp.assistant.core.services.DatabaseFactory.dbQuery
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class LocationRepo : LocationService {

    /**
     * This method saves a new lcoation on the system.
     * @param name is the name of the location.
     * @param postalCode is the postal code of the location.
     * @param position is the LatLng position of the location.
     */
    override fun newLocation (name: String, postalCode : Int, position : Position) {
        runBlocking {
            dbQuery {
                Locations.insert {
                    it[Locations.name] = name
                    it[Locations.postcode] = postalCode
                    it[Locations.lat] = position.lat
                    it[Locations.lon] = position.lon
                    it[Locations.province] = postalCode / 1000
                }
            }
        }
    }

    /**
     * This method retrieves a spanish province list.
     */
    override fun getProvinces(): List<Province> {
        return runBlocking {
            return@runBlocking dbQuery {
                return@dbQuery Provinces.selectAll().map {
                    Province(
                        code = it[Provinces.code],
                        name = it[Provinces.name],
                        locations = ArrayList()
                    )
                } as ArrayList
            }
        }
    }

    /**
     * This method retrieves the locations which are from the specified province by their province code.
     * @param provinceCode should be a integer number between 1 and 52.
     */
     override fun getLocations(provinceCode: Int): ArrayList<Location> {
        return runBlocking {
            return@runBlocking dbQuery {
                return@dbQuery Locations
                    .select({ Locations.province eq provinceCode })
                    .map {
                        Location(
                            name = it[Locations.name],
                            postcode = it[Locations.postcode],
                            latitude = it[Locations.lat],
                            longitude = it[Locations.lon],
                            people = ArrayList()
                        )
                    } as ArrayList<Location>
            }
        }
    }

    /**
     * Retrieves a specific location
     */
    override fun getLocationByPostalCode(postcode: Int): Location? {
        return runBlocking {
            return@runBlocking dbQuery {
                return@dbQuery Locations
                    .select({ Locations.postcode eq postcode })
                    .map {
                        Location(
                            name = it[Locations.name],
                            postcode = it[Locations.postcode],
                            latitude = it[Locations.lat],
                            longitude = it[Locations.lon],
                            people = ArrayList()
                        )
                    }.firstOrNull()
            }
        }
    }

}