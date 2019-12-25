package com.percomp.assistant.core.model

import com.percomp.assistant.core.util.Constants
import org.jetbrains.exposed.sql.Table

object Locations : Table(){
    val lat = double("lat")
    val lon = double("lon")
    val postcode = integer("postcode").primaryKey()
    val name = varchar("name", Constants.TOWN)
    val province = reference("province", Provinces.code)
}

object Provinces : Table(){
    val code = integer("code").primaryKey()
    val name = varchar("name", Constants.TOWN)
}

