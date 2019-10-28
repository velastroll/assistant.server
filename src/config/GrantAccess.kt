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
        "/api/conf/login" in uri    -> true
        "/oauth" in uri             -> true
        "login" in uri              -> true
        "/refreshtoken" in uri      -> true
        "/" == uri                  -> true
        "test" in uri               -> true
        "towns" in uri              -> true

        // only workers
        "/worker/" in uri           -> checkIfWorks(auth, UserType.USER)
        "/device/" in uri           -> checkIfWorks(auth, UserType.DEVICE)

        // everyone authenticated -> devices
        else                        ->
            (auth != null) && (checkAccessToken(auth.cleanTokenTag())!= null)
    }
}


/**
 * Private function to check if a request was done by a worker of the system.
 * @param [accessToken] should be the token of some type.
 * @param [type] to check.
 * @return [True] if the access token is nested for the inputted type.
 */
@KtorExperimentalAPI
private suspend fun checkIfWorks(accessToken: String?, type : UserType) : Boolean {
    return  (checkAccessToken(accessToken!!.cleanTokenTag()) == type)
}
