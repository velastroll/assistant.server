package com.percomp.assistant.core.controller.services

import com.percomp.assistant.core.dao.LocationDAO
import com.percomp.assistant.core.dao.UserDAO
import com.percomp.assistant.core.model.Person

class LocationCtrl {

    suspend fun add(name : String, postCode : Int){
        if (name.length < 3) throw IllegalArgumentException("Name is too short.")
        if (postCode < 1000) throw IllegalArgumentException("Invalid post code.")
        if (postCode > 52007) throw IllegalArgumentException("Invalid post code.")

        val province = postCode / 1000

        LocationDAO().post(name, postCode, province)
    }

    suspend fun retrieveAll(): List<Province> {

        // retrieve provinces
        val provinces = LocationDAO().getProvinces()

        // retrieve locations for each province
        for (p : Province in provinces){
            val locations = LocationDAO().get(code = p.code)
            // retrieve people for each location
            for (l : Location in locations){
                l.people = UserCtrl().retrieve(postalcode = l.postcode)
            }
            p.locations = locations
        }

        // return it
        return provinces
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
    var people : ArrayList<Person>
)
