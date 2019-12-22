package  com.percomp.assistant.core.config

import com.percomp.assistant.core.dao.UserDAO
import com.percomp.assistant.core.model.UserType
import io.ktor.util.KtorExperimentalAPI

/**
 * Grant access in function of authentication and uri to access.
 */
@KtorExperimentalAPI
suspend fun checkUri(uri : String, auth : String?) : Boolean {
    return when{

        // public operations: to log in
        "/api/conf/login" in uri    -> return true
        "/oauth" in uri             -> return true
        "login" in uri              -> return true
        "/refreshtoken" in uri      -> return true
        "/" == uri                  -> return true
        "test" in uri               -> return true
        "towns" in uri              -> return true

        // only workers
        "/worker/" in uri           -> return (checkAccessToken(UserType.USER, auth!!.cleanTokenTag()) != null)
        "/device/" in uri           -> return (checkAccessToken(UserType.DEVICE, auth!!.cleanTokenTag()) != null)

        // everyone authenticated -> devices
        else                        ->
            (auth != null) && (checkAccessToken(UserType.DEVICE, auth.cleanTokenTag()) != null)
    }
}

