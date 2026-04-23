package com.valladares.iptvplayer.core.network

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.serializer
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * Retrofit converter that streams JSON responses directly from the network InputStream
 * into kotlinx-serialization, avoiding the large intermediate String allocation that
 * the default Retrofit kotlinx-serialization converter performs via ResponseBody.string().
 *
 * This is critical for endpoints that return very large JSON payloads (e.g. Xtream VOD
 * catalogs with tens of thousands of entries), where the String allocation can exceed
 * the app heap.
 */
@OptIn(ExperimentalSerializationApi::class)
class StreamingJsonConverterFactory(
    private val json: Json,
    private val contentType: MediaType = "application/json".toMediaType()
) : Converter.Factory() {

    @Suppress("UNCHECKED_CAST")
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        val kSerializer = json.serializersModule.serializer(type) as KSerializer<Any?>
        return Converter<ResponseBody, Any?> { body ->
            body.use { responseBody ->
                responseBody.byteStream().use { stream ->
                    json.decodeFromStream(kSerializer, stream)
                }
            }
        }
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<Annotation>,
        methodAnnotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody>? = null
}
