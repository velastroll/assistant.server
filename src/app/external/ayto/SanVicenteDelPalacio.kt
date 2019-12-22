package com.percomp.assistant.core.controller.aytos

import com.percomp.assistant.core.config.backup.Logger
import com.percomp.assistant.core.controller.retriever.Address
import com.percomp.assistant.core.controller.retriever.Certificateless
import com.percomp.assistant.core.controller.retriever.Place
import com.percomp.assistant.core.controller.retriever.Towns
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.UnknownHostException


/**
 * All the class to retrieve any data with JSoup needs to be heritage of [Certificateless] to avoid repeat the step of
 * implementation of the [socketFactory] method.
 *
 * [SanVicenteDelPalacio] retrieve the data of their website, which is [Towns.SANVICENTEDELPALACIO.url] and put it in a
 * [Document]. It can be parsed with [Jsoup] and it's retrieved in [data], which is a JSon with the struct of [Place].
 *
 * @author Álvaro Velasco Gil
 */
class SanVicenteDelPalacio : Certificateless() {

    val log = Logger.instance;
    val town = Towns.SANVICENTEDELPALACIO
    var data = ArrayList<Place>() // contact data of the ayto

    init {
        try {
            data.add(updateContactData())
        }
        catch (e: UnknownHostException){
            log.error("Unavailable $town host: '${town.url}'")
        }
    }

    /**
     * Private fun to extract the document and put into [Place].
     * @return A place information.
     */
    private fun updateContactData() : Place {
        log.warn(" >> Updating $town: ${town.url}")
        val ayto : Document =
            Jsoup.connect(town.url).sslSocketFactory(socketFactory()).get() // website parsed to doc

        log.warn(" >> Received $town data.")
        val place = Place()
        // extract address
        log.warn(" >> Extracting $town data...")
        val address = Address()
        ayto.select("div .address .name")  // get name
            .forEach { element ->
                address.name = element.text()
            }

        ayto.select("div .address .street") // get street
            .forEach { element ->
                address.street = element.text()
            }

        ayto.select("div .address .postalcode") // get postal code
            .forEach { element ->
                address.postalcode = element.text()
            }

        ayto.select("div .address .city")  // get city
            .forEach { element ->
                address.city = element.text()
            }

        log.warn("Address: $address")
        place.address = address


        // extract data
        ayto.select("div .phones .local") // get telephone
            .forEach { element ->
                place.telephone = element.text()
            }
        ayto.select("div .phones .fax") // get fax
            .forEach { element ->
                place.fax = element.text()
            }
        ayto.select("div .email") // get email
            .forEach { element ->
                place.email = element.text()
            }
        ayto.select("div .urlExterna") // get email
            .forEach { element ->
                place.urlExterna = element.text()
            }
        log.warn("Place: $place")

        // return data
        return place
    }

    /**
     * Retrieve again the [data] of the Ayto from their website.
     * @return [doc] Contain the website parsed with the struct of [Document].
     */
    public fun reload() : Document {
        // get html from the imputed url
        val doc =  Jsoup.connect(town.url).sslSocketFactory(socketFactory()).get()
        while (doc == null ) {}
        // TODO: check if persist old occurrences to delete it in this case.
        data.add(updateContactData())
        return doc
    }

}

