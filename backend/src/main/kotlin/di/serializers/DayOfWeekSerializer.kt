package di.serializers

import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object DayOfWeekSerializer : KSerializer<DayOfWeek> {
    override val descriptor = PrimitiveSerialDescriptor("DayOfWeek", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): DayOfWeek =
        DayOfWeek.valueOf(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: DayOfWeek) {
        encoder.encodeString(value.name)
    }
}
