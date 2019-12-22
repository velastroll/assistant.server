package controller.services

import com.percomp.assistant.core.model.User

interface UserService{

    /**
     * TODO: add specification
     */
    fun newUser()

    /**
     * This method check if the combination of user and password is correct.
     * @param id is the user identifier, which is the same than the username.
     * @param password is the secret key of the user.
     * @return user information or null.
     */
    fun check(id : String, password : String) : User?

    /**
     * This emthod retrieves a specific user by their username.
     * @param id is the user identifier, which is the same than their username.
     * @return user information or null.
     */
    fun getUser(id : String) : User?

}