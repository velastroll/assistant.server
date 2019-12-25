package controller.services

import com.percomp.assistant.core.model.User

interface UserService{

    /**
     * Create a new user in the database.
     * @param signUp is the form to sign up in the system.
     */
    fun newUser(signUp: User, password: String?)

    /**
     * This method check if the combination of user and password is correct.
     * @param id is the user identifier, which is the same than the username.
     * @param password is the secret key of the user.
     * @return user information or null.
     */
    fun check(id : String, password : String) : User?

    /**
     * This method retrieves a specific user by their username.
     * @param id is the user identifier, which is the same than their username.
     * @return user information or null.
     */
    fun getUser(id : String) : User?

}