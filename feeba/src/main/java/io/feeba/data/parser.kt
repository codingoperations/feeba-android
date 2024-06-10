package io.feeba.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.Date

fun isEvent(triggerCondition: TriggerCondition): Boolean {
    return triggerCondition.type == RuleType.EVENT
}

fun isPageTrigger(ruleSet: RuleSet): Boolean {
    for (triggerCondition in ruleSet.triggers) {
        if (triggerCondition.type == RuleType.SCREEN) {
            return true
        }
    }
    return false
}

@Serializer(forClass = Date::class)
object DateSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("DateSerializer", PrimitiveKind.STRING)

    // parse it   "2024-06-08T18:53:00.501Z"
    private val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(format.format(value))
    }

    override fun deserialize(decoder: Decoder): Date {
        return format.parse(decoder.decodeString())
    }
}