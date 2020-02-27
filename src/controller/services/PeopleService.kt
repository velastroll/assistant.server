package controller.services

import com.percomp.assistant.core.model.Person
import com.percomp.assistant.core.model.Relation

interface PeopleService {

    /**
     * This method retrieves the info about a person with the specified nif.
     * @param nif is the national identifier of a specific person.
     * @return the person information or null.
     */
    fun getPerson(nif: String) : Person?

    /**
     * This method saves a new person on the system.
     * @param nif is the national identifier number of a specific person.
     * @param name is the user name.
     * @param postalCode is the postal code of the person location residence.
     */
    fun newPerson(nif: String, name : String, postalCode : Int)

    /**
     * This method retrieves the list which contains everyone who has registered on the system.
     * @return a person list.
     */
    fun getAllPeople() : List<Person>

    /**
     * This method retrieves everyone who has been registered with a specific postal code.
     * @param postalCode is the postal code of the person location residence.
     * @return a person list with the same postal code.
     */
    fun getByPostalCode(postalCode: Int) : List<Person>

    /**
     * This method retrieves the current relation of an user.
     * @param nif is the user national identifier.
     * @return current [Relation] or null.
     */
    fun getRelation(nif: String): Relation?
}