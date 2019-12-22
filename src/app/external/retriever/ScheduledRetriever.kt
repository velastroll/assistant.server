package com.percomp.assistant.core.controller.retriever



/**
 * TODO: schedule this task
 * This is an scheduled task, so its important that this execution will be on a non-blocking thread.
 */
class ScheduledRetriever {


    // for each town, retrieve their places
    private val masterTable = HashMap<Towns, ArrayList<Place>> ()

    /**
     * Constructor of the scheduled Retriever.
     */
    init {
        // retrieve the data in the moment when be instanced.

        // for each town, get their places
        for (t in Towns.values()){
            val townData = Retriever(t).data
            masterTable.put(t, townData)
        }
    }

    /**
     * Returns all the places relatives for a specific town.
     * @param [town] specified town
     * @return a list of [Place]
     */
    fun getPlaces(town: Towns) : ArrayList<Place>? {
        return masterTable[town]
    }

    /**
     * Retrieve again each accessible place of every towns
     */
    fun reloadAll(){
        // for each town, update their places
        for (t in Towns.values()){
            masterTable.remove(t)
            masterTable[t] = Retriever(t).data
        }
    }

    /**
     * Retrieve again all the places of a specific town.
     * @param town from where retrieve the data
     */
    fun reload(town: Towns){
        masterTable.remove(town)
        masterTable[town] = Retriever(town).data
    }


}