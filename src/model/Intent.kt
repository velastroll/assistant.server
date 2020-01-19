package model

import com.percomp.assistant.core.model.Devices
import com.percomp.assistant.core.util.Constants
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table


/* Data classes */

data class IntentData (
    var intentName : String,
    var confidenceScore : Double
)

data class SlotData (
    var confidence : Double,
    var raw_value : String,
    var entity : String? = null,
    var slotName : String
)

data class IntentDone (
    var datetime : String? = null,
    var intent : IntentData,
    var slots : List<SlotData>
)

data class Intent (
    var device : String,
    var datetime : String,
    var data : IntentData,
    var slots : List<SlotData>
)

/* Database classes */

object Intents : Table() {
    var id = varchar("id", Constants.IDENTIFIER).primaryKey()
    var datetime = varchar("datetime", Constants.DATETIME)
    var device = reference( "device", Devices.id)
    var intentName = varchar("intentName", Constants.INTENT_NAME)
    var confidence = double("confidence")
}

object Slots : Table () {
    var intent = reference("intent", Intents.id, onDelete = ReferenceOption.CASCADE)
    var confidence = double("confidence")
    var slotName = varchar("slotName", Constants.SLOT_NAME)
    var raw_value = varchar("raw_value", Constants.SLOT_RAW_VALUE)
    var entity = varchar("entity", Constants.SLOT_ENTITY).nullable()
}