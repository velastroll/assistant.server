package com.percomp.assistant.core.config


import com.percomp.assistant.core.controller.services.DeviceCtrl
import com.percomp.assistant.core.controller.services.UserCtrl
import com.percomp.assistant.core.model.UserType
import com.percomp.assistant.core.tokenStore
import io.ktor.auth.OAuth2Exception
import io.ktor.util.KtorExperimentalAPI
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.RefreshToken
import java.time.Instant

/**
 * Deletes the token prefix
 */
fun String.cleanTokenTag() : String{
    var a = ""
    if(this.contains("Bearer")) a = this.substring(7)
    if(this.contains("MAC")) a = this.substring(4)
    if(this.contains("Basic")) a = this.substring(6)
    return a
}

/**
 * Checks if an [access_token] is associated to any user/device.
 * @param [access_token] is a String
 * @return null if no user/device is nested for it.
 */
@Throws(OAuth2Exception.InvalidGrant::class)
@KtorExperimentalAPI
suspend fun checkAccessToken(access_token: String) : UserType?{
    val accessToken = tokenStore.accessToken(access_token) ?: throw OAuth2Exception.InvalidGrant("Invalid credentials")

    var toReturn = DeviceCtrl().exist(mac = accessToken.identity!!.username)
    if (toReturn == null) toReturn = UserCtrl().exist(accessToken.identity!!.username)
    return toReturn
}

/**
 * Updates the credentials nested for a specific user, and return it in a way of [Token].
 * Each [Token] includes two attributes: [access_token] and [refresh_token], which are [String].
 * @param [refresh_token] necessary to retrieve the user (or device) info and generate their new tokens.
 * @return [Token]
 */
@KtorExperimentalAPI
fun refreshTokens(refresh_token: String) : Token {

    // check if the refresh token is valid
    val rt = tokenStore!!.refreshToken(refresh_token.cleanTokenTag()) ?: throw OAuth2Exception.InvalidGrant("Bad request or invalid credentials")

    // create a new tokens
    val rt2 = RefreshToken(
        clientId = rt.clientId,
        expireTime = rt.expireTime.plusSeconds((60*60*24*10)),
        identity = rt.identity,
        scopes = rt.scopes,
        refreshToken = generateToken()
    )
    val at2 = AccessToken(
        clientId = rt2.clientId,
        expireTime = rt.expireTime.plusSeconds((60*60*24*2)),
        identity = rt.identity,
        refreshToken = rt2,
        scopes = rt.scopes,
        tokenType = "Bearer",
        accessToken = generateToken()
    )

    // revoke the older refresh token
    tokenStore.revokeRefreshToken(refresh_token.cleanTokenTag())

    // store the new tokens
    tokenStore.storeRefreshToken(rt2)
    tokenStore.storeAccessToken(at2)

    // return the token values
    return Token("Bearer ${at2.accessToken}", "Bearer ${rt2.refreshToken}")
}


/**
 * Generate [Token] which includes the both [access_token] as a [refresh_token] attributes, nested for a specific user.
 * Each att is a String which the scheme: 'Bearer XYZ' where 'XYZ' is substituting the real token.
 */
fun newTokens(username: String, clientId: String = "login") : Token{

    // retrieve the identity nested for a specific username
    val identity = Identity(username= username)

    // create a new tokens
    val rt = RefreshToken(
        clientId = clientId,
        expireTime = Instant.now().plusSeconds((60*60*24*10)),
        identity = identity,
        scopes = listOf("all").toSet(),
        refreshToken = generateToken()
    )
    val at = AccessToken(
        clientId = rt.clientId,
        expireTime = Instant.now().plusSeconds((60*30)),
        identity = rt.identity,
        refreshToken = rt,
        scopes = listOf("all").toSet(),
        tokenType = "Bearer",
        accessToken = generateToken()
    )

    // store the new tokens
    tokenStore.storeRefreshToken(rt)
    tokenStore.storeAccessToken(at)

    // return it
    return Token("Bearer ${at.accessToken}", "Bearer ${rt.refreshToken}")
}

/**
 * Data class to represent a couple of token.
 */
data class Token(val access_token: String, val refresh_token: String?)


// Tools to generate the token
private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
private const val TOKEN_LENGTH = 32
private fun generateToken() = (1..TOKEN_LENGTH)
        .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")


