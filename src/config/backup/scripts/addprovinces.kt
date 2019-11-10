package com.percomp.assistant.core.config.backup.scripts

import com.percomp.assistant.core.domain.Provinces
import org.jetbrains.exposed.sql.insert

fun addprovinces() {

    Provinces.insert { it[Provinces.code] = 1; it[Provinces.name] = "Álava" }
    Provinces.insert { it[Provinces.code] = 2; it[Provinces.name] = "Albacete" }
    Provinces.insert { it[Provinces.code] = 3; it[Provinces.name] = "Alicante" }
    Provinces.insert { it[Provinces.code] = 4; it[Provinces.name] = "Almería" }
    Provinces.insert { it[Provinces.code] = 5; it[Provinces.name] = "Ávila" }
    Provinces.insert { it[Provinces.code] = 6; it[Provinces.name] = "Badajoz" }
    Provinces.insert { it[Provinces.code] = 7; it[Provinces.name] = "Baleares" }
    Provinces.insert { it[Provinces.code] = 8; it[Provinces.name] = "Barcelona" }
    Provinces.insert { it[Provinces.code] = 9; it[Provinces.name] = "Burgos" }
    Provinces.insert { it[Provinces.code] = 10; it[Provinces.name] = "Cáceres" }
    Provinces.insert { it[Provinces.code] = 11; it[Provinces.name] = "Cádiz" }
    Provinces.insert { it[Provinces.code] = 12; it[Provinces.name] = "Castellón" }
    Provinces.insert { it[Provinces.code] = 13; it[Provinces.name] = "Ciudad Real" }
    Provinces.insert { it[Provinces.code] = 14; it[Provinces.name] = "Córdoba" }
    Provinces.insert { it[Provinces.code] = 15; it[Provinces.name] = "Coruña" }
    Provinces.insert { it[Provinces.code] = 16; it[Provinces.name] = "Cuenca" }
    Provinces.insert { it[Provinces.code] = 17; it[Provinces.name] = "Gerona" }
    Provinces.insert { it[Provinces.code] = 18; it[Provinces.name] = "Granada" }
    Provinces.insert { it[Provinces.code] = 19; it[Provinces.name] = "Guadalajara" }
    Provinces.insert { it[Provinces.code] = 20; it[Provinces.name] = "Guipúzcua" }
    Provinces.insert { it[Provinces.code] = 21; it[Provinces.name] = "Huelva" }
    Provinces.insert { it[Provinces.code] = 22; it[Provinces.name] = "Huesca" }
    Provinces.insert { it[Provinces.code] = 23; it[Provinces.name] = "Jaén" }
    Provinces.insert { it[Provinces.code] = 24; it[Provinces.name] = "León" }
    Provinces.insert { it[Provinces.code] = 25; it[Provinces.name] = "Lérida" }
    Provinces.insert { it[Provinces.code] = 26; it[Provinces.name] = "La Rioja" }
    Provinces.insert { it[Provinces.code] = 27; it[Provinces.name] = "Lugo" }
    Provinces.insert { it[Provinces.code] = 28; it[Provinces.name] = "MAdrid" }
    Provinces.insert { it[Provinces.code] = 29; it[Provinces.name] = "Málaga" }
    Provinces.insert { it[Provinces.code] = 30; it[Provinces.name] = "Murcia" }
    Provinces.insert { it[Provinces.code] = 31; it[Provinces.name] = "Navarra" }
    Provinces.insert { it[Provinces.code] = 32; it[Provinces.name] = "Orense" }
    Provinces.insert { it[Provinces.code] = 33; it[Provinces.name] = "Asturias" }
    Provinces.insert { it[Provinces.code] = 34; it[Provinces.name] = "Palencia" }
    Provinces.insert { it[Provinces.code] = 35; it[Provinces.name] = "Las Palmas" }
    Provinces.insert { it[Provinces.code] = 36; it[Provinces.name] = "Pontevedra" }
    Provinces.insert { it[Provinces.code] = 37; it[Provinces.name] = "Salamanca" }
    Provinces.insert { it[Provinces.code] = 38; it[Provinces.name] = "Santa Cruz de Tenerife" }
    Provinces.insert { it[Provinces.code] = 39; it[Provinces.name] = "Cantabria" }
    Provinces.insert { it[Provinces.code] = 40; it[Provinces.name] = "Segovia" }
    Provinces.insert { it[Provinces.code] = 41; it[Provinces.name] = "Sevilla" }
    Provinces.insert { it[Provinces.code] = 42; it[Provinces.name] = "Soria" }
    Provinces.insert { it[Provinces.code] = 43; it[Provinces.name] = "Tarragona" }
    Provinces.insert { it[Provinces.code] = 44; it[Provinces.name] = "Teruel" }
    Provinces.insert { it[Provinces.code] = 45; it[Provinces.name] = "Toledo" }
    Provinces.insert { it[Provinces.code] = 46; it[Provinces.name] = "Valencia" }
    Provinces.insert { it[Provinces.code] = 47; it[Provinces.name] = "Valladolid" }
    Provinces.insert { it[Provinces.code] = 48; it[Provinces.name] = "Vizcaya" }
    Provinces.insert { it[Provinces.code] = 49; it[Provinces.name] = "Zamora" }
    Provinces.insert { it[Provinces.code] = 50; it[Provinces.name] = "Zaragoza" }
    Provinces.insert { it[Provinces.code] = 51; it[Provinces.name] = "Ceuta" }
    Provinces.insert { it[Provinces.code] = 52; it[Provinces.name] = "Melilla" }
}