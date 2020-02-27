package com.percomp.assistant.core.app.config.oauth

import com.percomp.assistant.core.controller.domain.DeviceCtrl
import com.percomp.assistant.core.controller.domain.UserCtrl
import com.percomp.assistant.core.controller.services.LocationService
import com.percomp.assistant.core.model.UserType
import com.percomp.assistant.core.rest.log
import com.percomp.assistant.core.tokenStore
import controller.services.*
import io.ktor.auth.OAuth2Exception
import io.ktor.util.KtorExperimentalAPI
import model.Token
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.RefreshToken
import java.time.Instant

class TokenCtrl(
    dS : DeviceService,
    tS : TaskService,
    pS : PeopleService,
    lS : LocationService,
    iS : IntentsService,
    uS : UserService
) : AuthService {

    private val deviceCtrl = DeviceCtrl(dS, tS, pS, lS, this, iS)
    private val userCtrl = UserCtrl(uS, pS, dS, this)

    /**
     * Deletes the token prefix
     */
    override fun cleanTokenTag(token : String) : String{
        var a = ""
        if(token.contains("Bearer")) a = token.substring(7)
        if(token.contains("MAC")) a = token.substring(4)
        if(token.contains("Basic")) a = token.substring(6)
        return a
    }

    /**
     * Checks if an [access_token] is associated to any user/device.
     * @param [access_token] is a String
     * @return null if no user/device is nested for it.
     */
    @Throws(OAuth2Exception.InvalidGrant::class)
    @KtorExperimentalAPI
    override fun checkAccessToken(device : UserType, access_token: String) : String? {
        val accessToken = tokenStore.accessToken(access_token) ?: throw OAuth2Exception.InvalidGrant("Unarchived token.")
        log.debug("${access_token} => AT: $accessToken")
        val toReturn = deviceCtrl.exist(mac = accessToken.identity!!.username)
        log.debug("toReturn: $toReturn")
        if (toReturn != null && device == UserType.DEVICE) {

            log.debug("return: ${toReturn.mac}")
            return toReturn.mac
        }
        else {
            log.debug("check if user")
            val user = userCtrl.exist(accessToken.identity!!.username)
            log.debug("user: $user")
            if (user == null) return null
            else return user.username
        }
    }

    /**
     * Updates the credentials nested for a specific user, and return it in a way of [Token].
     * Each [Token] includes two attributes: [access_token] and [refresh_token], which are [String].
     * @param [refresh_token] necessary to retrieve the user (or device) info and generate their new tokens.
     * @return [Token]
     */
    @KtorExperimentalAPI
    override fun refreshTokens(refresh_token: String) : Token {

        // check if the refresh token is valid
        val rt = tokenStore.refreshToken(cleanTokenTag(refresh_token)) ?: throw OAuth2Exception.InvalidGrant("Bad request or invalid credentials")

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
        tokenStore.revokeRefreshToken(cleanTokenTag(refresh_token))

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
    override fun newTokens(username: String, clientId: String) : Token{

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



}


// Tools to generate the token
private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
private val TOKEN_LENGTH = 32
private fun generateToken() = (1..TOKEN_LENGTH)
        .map { kotlin.random.Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
