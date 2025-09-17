package com.kumbra.crm.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull


/** Acepta "texto", false o null -> devuelve String? */
object StringOrFalseAsNullSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("StringOrFalseAsNull", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String? {
        val jd = decoder as? JsonDecoder ?: return null
        val el: JsonElement = jd.decodeJsonElement()
        return when (el) {
            is JsonPrimitive -> {
                when {
                    el.isString -> el.content
                    el.booleanOrNull == false -> null
                    else -> el.toString()
                }
            }
            JsonNull -> null
            else -> null
        }
    }
    override fun serialize(encoder: Encoder, value: String?) {
        if (value == null) encoder.encodeNull() else encoder.encodeString(value)
    }
}

@Serializable
data class Lead(
    val id: Int,
    val name: String,
    val contact_name: String? = null,
    val first_name: String? = null,
    val last_name: String? = null,
    @Serializable(with = StringOrFalseAsNullSerializer::class)
    val email: String? = null,
    @Serializable(with = StringOrFalseAsNullSerializer::class)
    val phone: String? = null,
    @Serializable(with = StringOrFalseAsNullSerializer::class)
    val mobile: String? = null,
    val country: String? = null,
    val tags: List<String> = emptyList(),
    val create_date: String? = null,
)
@Serializable data class LeadList(val items: List<Lead> = emptyList())
@Serializable data class LeadQuery(
    val limit: Int = 50,
    val tag: String? = null,
    val email: String? = null,
    val from: String? = null,
    val to: String? = null,
)

@Serializable
data class Contact(
    val id: Int,
    val name: String,
    val first_name: String? = null,
    val last_name: String? = null,
    @Serializable(with = StringOrFalseAsNullSerializer::class)
    val email: String? = null,
    @Serializable(with = StringOrFalseAsNullSerializer::class)
    val phone: String? = null,
    @Serializable(with = StringOrFalseAsNullSerializer::class)
    val mobile: String? = null,
)
@Serializable data class ContactList(val items: List<Contact> = emptyList())
@Serializable data class ContactQuery(val limit: Int = 50, val email: String? = null)

@Serializable data class RpcEnvelope<T>(val jsonrpc: String = "2.0", val method: String = "call", val params: T)
@Serializable data class RpcResponse<T>(val result: T)
