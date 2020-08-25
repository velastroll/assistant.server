package com.percomp.assistant.core.controller.services

import com.percomp.assistant.core.controller.domain.Location
import com.percomp.assistant.core.controller.domain.Province
import com.percomp.assistant.core.model.Position

interface LocationService {

    /**
     * This method saves a new lcoation on the system.
     * @param name is the name of the location.
     * @param postalCode is the postal code of the location.
     * @param position is the LatLng position of the location.
     */
    fun newLocation(name : String, postalCode : Int, position : Position)

    /**
     * This method retrieves a spanish province list.
     */
    fun getProvinces() : List<Province>

    /**
     * This method retrieves the registered location filtered by it spanish postal code.
     * @param postalCode should be a integer number between 1000 and 53000.
     */
    fun getLocationByPostalCode(postalCode: Int) : Location?

    /**
     * This method retrieves the locations which are from the specified province by their province code.
     * @param provinceCode should be a integer number between 1 and 52.
     */
    fun getLocations(provinceCode : Int) : List<Location>
}