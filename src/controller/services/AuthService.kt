package controller.services

import com.percomp.assistant.core.model.UserType
import io.ktor.auth.OAuth2Exception
import io.ktor.util.KtorExperimentalAPI
import model.Token

interface AuthService{

    /**
     * Deletes the token prefix
     */
    fun cleanTokenTag(token : String) : String

    /**
     * Checks if an [access_token] is associated to any user/device.
     * @param [access_token] is a String
     * @return null if no user/device is nested for it.
     */
    @Throws(OAuth2Exception.InvalidGrant::class)
    @KtorExperimentalAPI
    fun checkAccessToken(device : UserType, access_token: String) : String?

    /**
     * Updates the credentials nested for a specific user, and return it in a way of [Token].
     * Each [Token] includes two attributes: [access_token] and [refresh_token], which are [String].
     * @param [refresh_token] necessary to retrieve the user (or device) info and generate their new tokens.
     * @return [Token]
     */
    @KtorExperimentalAPI
    @Throws(OAuth2Exception.InvalidGrant::class)
    fun refreshTokens(refresh_token: String) : Token

    /**
     * Generate [Token] which includes the both [access_token] as a [refresh_token] attributes, nested for a specific user.
     * Each att is a String which the scheme: 'Bearer XYZ' where 'XYZ' is substituting the real token.
     */
    fun newTokens(username: String, clientId: String = "login") : Token
}
