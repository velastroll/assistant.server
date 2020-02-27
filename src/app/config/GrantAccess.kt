package  com.percomp.assistant.core.app.config

import com.percomp.assistant.core.model.UserType
import controller.services.AuthService
import io.ktor.util.KtorExperimentalAPI
import org.koin.core.KoinComponent
import org.koin.core.inject


class GrantAccessCtrl : KoinComponent{

    private val auth: AuthService by inject()

    /**
     * Grant access in function of authentication and uri to access.
     */
    @KtorExperimentalAPI
    fun checkUri(uri: String, tkn: String?): Boolean {
        return when {

            // public operations: to log in
            "/api/conf/login" in uri -> return true
            "/oauth" in uri -> return true
            "login" in uri -> return true
            "/refreshtoken" in uri -> return true
            "/" == uri -> return true
            "test" in uri -> return true
            "towns" in uri -> return true

            // only workers
            "/worker/" in uri -> return (auth.checkAccessToken(UserType.USER, auth.cleanTokenTag(tkn!!)) != null)
            "/device/" in uri -> return (auth.checkAccessToken(UserType.DEVICE, auth.cleanTokenTag(tkn!!)) != null)

            // everyone authenticated -> devices
            else ->
                (tkn != null) && (auth.checkAccessToken(UserType.DEVICE, auth.cleanTokenTag(tkn)) != null)
        }
    }
}

