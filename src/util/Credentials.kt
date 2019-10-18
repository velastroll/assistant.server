package com.percomp.assistant.core.util

/**
 * Enum class to doesn't push to git specific values in a unsafe way.
 *
 * File .gitignore should contain this class.
 */
enum class Credentials(val value : String){
    PORT("8082")
}
