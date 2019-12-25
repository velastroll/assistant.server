package com.percomp.assistant.core.config.oauth

import nl.myndocs.oauth2.tokenstore.inmemory.InMemoryTokenStore

/**
 * Singleton pattern:
 * Only one instance of InMemoryTokenStore is being used,
 * so we can access to the object instanced in the OAuthServer configuration to
 * managing the different access tokens.
 */
class InMemoryTokenStoreCustom{

    companion object {
        private var tokenStore : InMemoryTokenStore? = null

        fun get() : InMemoryTokenStore {
            if ( tokenStore == null ) {
                tokenStore = InMemoryTokenStore()
            }
            return tokenStore as InMemoryTokenStore
        }
    }
}
