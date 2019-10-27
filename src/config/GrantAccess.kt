package  com.percomp.assistant.core.config

import com.percomp.assistant.core.dao.UserDAO
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
        "towns" in uri               -> true

        // only workers
        "/worker/" in uri           -> checkIfWorks(auth)

        // everyone authenticated -> devices
        else                        ->
            (auth != null) && (checkAccessToken(auth.cleanTokenTag())!= null)
    }
}


/**
 * Private function to check if a request was done by a worker of the system.
 * @param [accessToken] should be the token of some worker.
 * @return [True] if the access token is nested for a worker.
 */
@KtorExperimentalAPI
private suspend fun checkIfWorks(accessToken: String?) : Boolean {

    if (accessToken == null) return false

    // if [accessToken] hasn't got the struct of a token, return false, else, get the user/device nested for it
    val identifier = checkAccessToken(accessToken.cleanTokenTag()) ?: return false

    // check if identifier exist al worker
    return UserDAO().checkExists(identifier)
}
