package com.percomp.assistant.core.util

object Constants {
    val TIMESTAMP = 25
    val IDENTIFIER = 17
    val SALT = 16
    val NAME = 16
    val SURNAME = 64
    val USERNAME = 16
    val PASSWORD = 16
    const val SHA_PASSWORD: Int = 512
    val CHARPOOL: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9') + '.' + '*' + '_' + '-'
    val NORMALCHARPOOL: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    val HEX : List<Char> = ('a'..'f') + ('A'..'F') + ('0'..'9')
    val TOWN: Int = 64
}