package com.valladares.iptvplayer.data.xtream.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Xtream-leverantörer skickar ofta samma fält med olika typer
 * (String vs Number vs tom sträng). Dessa serializers normaliserar
 * inkonsekvent payload vid deserialisering.
 */
object FlexibleDoubleSerializer : KSerializer<Double?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleDouble", PrimitiveKind.DOUBLE).nullable

    override fun deserialize(decoder: Decoder): Double? {
        require(decoder is JsonDecoder)
        val element = decoder.decodeJsonElement()
        if (element is JsonNull) {
            return null
        }
        val content = (element as? JsonPrimitive)?.contentOrNull ?: return null
        if (content.isBlank() || content.equals("N/A", ignoreCase = true)) {
            return null
        }
        return content.toDoubleOrNull()
    }

    override fun serialize(encoder: Encoder, value: Double?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeDouble(value)
        }
    }
}

/**
 * Lenient Int deserializer that accepts numbers, numeric strings and null.
 */
object FlexibleIntSerializer : KSerializer<Int?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleInt", PrimitiveKind.INT).nullable

    override fun deserialize(decoder: Decoder): Int? {
        require(decoder is JsonDecoder)
        val element = decoder.decodeJsonElement()
        if (element is JsonNull) {
            return null
        }
        val content = (element as? JsonPrimitive)?.contentOrNull ?: return null
        if (content.isBlank() || content.equals("N/A", ignoreCase = true)) {
            return null
        }
        return content.toIntOrNull()
    }

    override fun serialize(encoder: Encoder, value: Int?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeInt(value)
        }
    }
}

/**
 * Lenient String deserializer that accepts string/number/boolean payloads.
 */
object FlexibleStringSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleString", PrimitiveKind.STRING).nullable

    override fun deserialize(decoder: Decoder): String? {
        require(decoder is JsonDecoder)
        val element = decoder.decodeJsonElement()
        if (element is JsonNull) {
            return null
        }
        val content = (element as? JsonPrimitive)?.contentOrNull ?: return null
        if (content.isBlank()) {
            return null
        }
        return content
    }

    override fun serialize(encoder: Encoder, value: String?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeString(value)
        }
    }
}
