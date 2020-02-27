package model

/**
 * Data class to represent a couple of token.
 */
data class Token(
    val access_token: String,
    val refresh_token: String?
)