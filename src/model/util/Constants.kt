package com.percomp.assistant.core.util

object Constants {
    val TIMESTAMP = 28
    val IDENTIFIER = 17
    val SALT = 16
    val NAME = 16
    val SURNAME = 64
    val USERNAME = 16
    val PASSWORD = 16
    const val SHA_PASSWORD: Int = 512
    const val EVENT_CONTENT: Int = 1024
    val CHARPOOL: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9') + '.' + '*' + '_' + '-'
    val NORMALCHARPOOL: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    val HEX : List<Char> = ('a'..'f') + ('A'..'F') + ('0'..'9')
    val TOWN: Int = 64
    val DATETIME: Int = 28
    val INTENT_NAME: Int = 32
    val SLOT_NAME: Int = 32
    val SLOT_ENTITY: Int = 32
    val SLOT_RAW_VALUE: Int = 32
}