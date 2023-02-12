package com.rokoblak.chatbackup.services

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream
import javax.inject.Inject

@OptIn(ExperimentalSerializationApi::class)
class JsonSerializer @Inject constructor() {

    val json = Json {
        isLenient =
            true // Reason: In case some formats don't conform to the strict RFC-4627 standard
        ignoreUnknownKeys = true // Allows to specify minimal models
        encodeDefaults = true // Default parameters are still encoded
        explicitNulls =
            false // Nulls are not encoded. Decode absent values into nulls if no default set.
    }

    fun <T> encode(serializer: SerializationStrategy<T>, data: T) =
        json.encodeToString(serializer, data)

    fun <T> decode(serializer: DeserializationStrategy<T>, data: String) =
        json.decodeFromString(serializer, data)

    fun <T> decodeStream(serializer: DeserializationStrategy<T>, data: InputStream) =
        json.decodeFromStream(serializer, data)
}