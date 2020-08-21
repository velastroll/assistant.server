package com.percomp.assistant.core.services

import com.percomp.assistant.core.controller.services.IntentsService
import com.percomp.assistant.core.model.*
import com.percomp.assistant.core.services.DatabaseFactory.dbQuery
import com.percomp.assistant.core.util.Constants
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class IntentRepo : IntentsService {

    /**
     * Retrieve the last intent of a specific device.
     * @param mac is the device identifier
     */
    override fun getLastIntent(mac: String): Intent4W? {
        return runBlocking {
            return@runBlocking dbQuery {
                Intents
                    .select({
                        Intents.device eq mac})
                    .orderBy(Intents.datetime, isAsc = false) // ordered by Intents
                    .map {
                        Intent4W(
                            intent = it[Intents.intentName],
                            timestamp = it[Intents.datetime]
                        )
                    }.firstOrNull()
            }
        }
    }

    /**
     * This method retrieves a list with all the intents of a specific device registered on an interval of dates, and
     * ordered by intent name.
     * @param device is the identifier of the device.
     * @param from is the min Intent datetime to retrieve.
     * @param to is the max intent datetime to retrieve.
     */
    override fun getIntents(device: String, from: String, to: String): List<Intent> {
        return runBlocking {
            return@runBlocking dbQuery {

                // Retrieve intent
                return@dbQuery Intents
                    .select({
                        Intents.device eq device and (
                        Intents.datetime greaterEq from and (
                        Intents.datetime lessEq to))})
                    .orderBy(Intents.intentName, isAsc = false) // ordered by Intents
                    .map {

                        val slots = Slots
                            .select({Slots.intent eq it[Intents.id]})
                            .map{
                                SlotData(
                                    slotName = it[Slots.slotName],
                                    confidence = it[Slots.confidence],
                                    raw_value = it[Slots.raw_value],
                                    entity = it[Slots.entity])
                            }

                        Intent(
                            device = it[Intents.device],
                            datetime = it[Intents.datetime],
                            data = IntentData(
                                intentName = it[Intents.intentName],
                                confidenceScore = it[Intents.confidence]),
                            slots = slots
                        )
                    }
            }
        }
    }

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