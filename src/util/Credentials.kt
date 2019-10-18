package com.percomp.assistant.core.util

/**
 * Enum class to doesn't push to git specific values in a unsafe way.
 *
 * File .gitignore should contain this class.
 */
enum class Credentials(val value : String){
    PORT("8082"),
    BASEURL("localhost"),


    // OAUTH
    OAUTH_CONF("raspsistant"),
    OAUTH_CLIENTID("raspi-pregonero"),
    OAUTH_CLIENTSECRET("h3yJuli4"),
    OAUTH_ACCESSTOKEN_TIME("${24*60*60}"), // one day
    OAUTH_REFRESHTOKEN_TIME("${7*24*60*60}"), // one week
}
