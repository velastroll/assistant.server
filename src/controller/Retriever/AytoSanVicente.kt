package com.percomp.assistant.core.controller.Retriever

import org.jsoup.Jsoup


import java.security.KeyManagementException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.security.cert.CertificateException


class Retriever(town : TOWNS){

    val data = Place()

    init {

        // get html from the imputed url
        val doc = Jsoup.connect(town.url).sslSocketFactory(socketFactory()).get()    // <1>

        // extract address
        val address = Address()
        doc.select("div .address .name")  // get name
            .forEach { element ->
                address.name = element.text()
            }

        doc.select("div .address .street") // get street
            .forEach { element ->
                address.street = element.text()
            }

        doc.select("div .address .postalcode") // get postal code
            .forEach { element ->
                address.postalcode = element.text()
            }

        doc.select("div .address .city")  // get city
            .forEach { element ->
                address.city = element.text()
            }
        data.address = address


        // extract data
        doc.select("div .phones .local") // get telephone
            .forEach { element ->
                data.telephone = element.text()
            }
        doc.select("div .phones .fax") // get fax
            .forEach { element ->
                data.fax = element.text()
            }
        doc.select("div .email") // get email
            .forEach { element ->
                data.email = element.text()
            }
        doc.select("div .urlExterna") // get email
            .forEach { element ->
                data.urlExterna = element.text()
            }

        println(data)

    }

    private fun socketFactory(): SSLSocketFactory {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate>? {
                return arrayOf()
            }
        })

        try {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            return sslContext.socketFactory
        } catch (e: Exception) {
            when (e) {
                is RuntimeException, is KeyManagementException -> {
                    throw RuntimeException("Failed to create a SSL socket factory", e)
                }
                else -> throw e
            }
        }
    }

}


/**
 * Data class to retrieve the contact of a specific Ayto.
 */
data class Place(
    var address: Address? = null,
    var telephone: String? = null,
    var fax:String? = null,
    var email: String? = null,
    var url: String? = null,
    var urlExterna: String? = null
)

data class Address(
    var name : String? = null,
    var street: String? = null,
    var postalcode : String? = null,
    var city : String? = null
)

enum class TOWNS(val url : String) {

    SANVICENTEDELPALACIO("https://sanvicentedelpalacio.ayuntamientosdevalladolid.es");

}