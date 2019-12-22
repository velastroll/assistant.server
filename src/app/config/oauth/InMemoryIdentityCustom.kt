package com.percomp.assistant.core.config.oauth

import kotlinx.coroutines.runBlocking
import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.identity.inmemory.IdentityConfiguration

class InMemoryIdentityCustom : IdentityService {
    private val identities = mutableListOf<IdentityConfiguration>()
    private var imic: InMemoryIdentityCustom? = null

    fun identity(inline: IdentityConfiguration.() -> Unit): InMemoryIdentityCustom {
        val client = IdentityConfiguration()
        inline(client)

        identities.add(client)
        if (imic == null) imic = this
        return imic as InMemoryIdentityCustom
    }

    fun singleton() : InMemoryIdentityCustom{
        if (imic == null) imic = this
        return imic as InMemoryIdentityCustom
    }

    override fun identityOf(forClient: Client, username: String): Identity? {
        val findConfiguration = findConfiguration(username)

        if (findConfiguration == null) {
            return null
        }

        return Identity(
            findConfiguration.username!!
        )
    }

    override fun allowedScopes(forClient: Client, identity: Identity, scopes: Set<String>) = scopes

    override fun validCredentials(forClient: Client, identity: Identity, password: String): Boolean {
        try {
            val b = runBlocking {
                // TODO: replace this: if is correct the combination on user+password
                val exist =  false // UserDAO().check(username = identity.username, password = password) != null
                return@runBlocking exist
            }
            return b
        } catch (e: Exception) { return false }
    }

    private fun findConfiguration(username: String): IdentityConfiguration? {
        try {
            return runBlocking {
                // TODO: replace this: username exist on db
                if (true) IdentityConfiguration(username)
                else null
            }
        } catch (e: Exception) { return null}
    }

}
