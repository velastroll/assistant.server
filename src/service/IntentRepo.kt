package service

import com.percomp.assistant.core.dao.DatabaseFactory.dbQuery
import com.percomp.assistant.core.util.Constants
import controller.services.IntentsService
import kotlinx.coroutines.runBlocking
import model.Intent
import model.Intents
import model.Slots
import org.jetbrains.exposed.sql.insert

class IntentRepo : IntentsService {

    /**
     * This method stores an already done intent by a specific device.
     * The intent contains the list of involved slots.
     * @param data is the intent data. [Intent]
     */
    override fun addIntentAction(data: Intent) {

        // generate id
        val id = generateId()

        runBlocking {
            dbQuery {
                Intents.insert {
                    it[Intents.id] = id
                    it[Intents.device] = data.device
                    it[Intents.datetime] = data.datetime
                    it[Intents.confidence] = data.data.confidenceScore
                    it[Intents.intentName] = data.data.intentName
                }

                for (s in data.slots) {
                    Slots.insert {
                        it[Slots.intent] = id
                        it[Slots.slotName] = s.slotName
                        it[Slots.confidence] = s.confidence
                        it[Slots.entity] = s.entity
                        it[Slots.raw_value] = s.raw_value
                    }
                }
            }
        }
    }



    private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private fun generateId() = (1..Constants.IDENTIFIER)
            .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
}