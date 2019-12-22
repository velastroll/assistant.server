package com.percomp.assistant.core.controller.retriever

import com.percomp.assistant.core.controller.aytos.SanVicenteDelPalacio


/**
 * Controller to retrieve the data of a specific town.
 */
class Retriever(town : Towns){

    var data = ArrayList<Place>()

    init {
        when(town){
            Towns.SANVICENTEDELPALACIO -> {
                data = SanVicenteDelPalacio().data
            }
            // other
        }
    }

}


/**
 * Data class to retrieve the contact of a specific site.
 */
data class Place(
    var address: Address? = null,
    var telephone: String? = null,
    var fax:String? = null,
    var email: String? = null,
    var url: String? = null,
    var urlExterna: String? = null
)

/**
 * Representation of a specific address.
 */
data class Address(
    var name : String? = null,
    var street: String? = null,
    var postalcode : String? = null,
    var city : String? = null
)


/**
 * List of websites to retrieve their data.
 */
public enum class Towns(val url : String) {

    SANVICENTEDELPALACIO("http://sanvicentedelpalacio.ayuntamientosdevalladolid.es");

}
