package com.percomp.assistant.core.controller.retriever


/**
 * Singleton pattern:
 * Only one instance of [ScheduledRetriever] is being used,
 * so we can access to the scheduled data.
 */
class IScheduledRetriever{

    companion object {
        private var retrieverInstance : ScheduledRetriever? = null

        fun init(){
            if ( retrieverInstance == null ) {
                retrieverInstance = ScheduledRetriever()
            }
        }

        fun get(town: Towns) : ArrayList<Place> {
            if ( retrieverInstance == null ) {
                retrieverInstance = ScheduledRetriever()
            }
            return retrieverInstance!!.getPlaces(town) as ArrayList<Place>
        }

        fun reload(){
            retrieverInstance = ScheduledRetriever()
        }
    }
}