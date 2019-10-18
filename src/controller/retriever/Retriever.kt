package com.percomp.assistant.core.controller.Retriever

import com.percomp.assistant.core.controller.aytos.SanVicenteDelPalacio
import org.jsoup.Jsoup

import java.security.KeyManagementException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.security.cert.CertificateException


class Retriever(town : Towns){

    var data = Place()

    init {
        when(town){
            Towns.SANVICENTEDELPALACIO -> data = SanVicenteDelPalacio().data
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

public enum class Towns(val url : String) {

    SANVICENTEDELPALACIO("https://sanvicentedelpalacio.ayuntamientosdevalladolid.es");

}
