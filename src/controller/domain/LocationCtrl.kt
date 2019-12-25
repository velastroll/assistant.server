package com.percomp.assistant.core.controller.services

import com.percomp.assistant.core.model.Person
import com.percomp.assistant.core.model.Position
import controller.services.PeopleService

class LocationCtrl (
    private val locationService : LocationService,
    private val peopleService: PeopleService

){

    fun add(name : String, postCode : Int, lat: Double, lon: Double){
        if (name.length < 3) throw IllegalArgumentException("Name is too short.")
        if (postCode < 1000) throw IllegalArgumentException("Invalid post code.")
        if (postCode > 52007) throw IllegalArgumentException("Invalid post code.")
        if (lat > 90) throw IllegalArgumentException("Invalid latitude: $lat.")
        if (lat < -90) throw IllegalArgumentException("Invalid latitude: $lat.")
        if (lon > 180) throw IllegalArgumentException("Invalid longitude: $lon.")
        if (lon < -180) throw IllegalArgumentException("Invalid longitude: $lon.")

        locationService.newLocation(name, postCode, Position(lat, lon))
    }

    fun retrieveAll(): List<Province> {

        // retrieve provinces
        val provinces = locationService.getProvinces()

        // retrieve locations for each province
        for (p : Province in provinces){
            val locations = locationService.getLocations(provinceCode = p.code)
            // retrieve people for each location
            for (l : Location in locations){
                l.people = peopleService.getByPostalCode(postalCode = l.postcode) as ArrayList<Person>
            }
            p.locations = locations as ArrayList<Location>
        }

        // return it
        return provinces
    }

    fun retrieve(postcode: String?): Location?{
        return locationService.getLocationByPostalCode(postalCode = postcode!!.toInt())
    }
}

data class Province (
    val code : Int,
    val name : String,
    var locations : ArrayList<Location>

)

data class Location(
    val name : String,
    val postcode : Int,
    var latitude : Double,
    var longitude : Double,
    var people : ArrayList<Person>
)
