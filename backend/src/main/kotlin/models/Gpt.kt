package ru.itmo.ivandor.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DataClass(
    val result: Inn,
)
@Serializable
data class Inn(
    val alternatives: List<Ott>,
)
@Serializable
data class Ott(
    val message: Mess,
)
@Serializable
data class Mess(
    val role: String,
    val text: String,
)

@Serializable
data class IdealJson(
    val text: String
)

@Serializable
data class Req(
    val modelUri: String,
    val completionOptions: Conf,
    val messages: List<Message>,
    @SerialName("json_schema")
    val jsonSchema: JsonSchema
)


@Serializable
data class Conf(
    val stream: Boolean,
    val temperature: Double,
    val maxTokens: Int,
    val reasoningOptions: ReasoningOptions
)

@Serializable
data class ReasoningOptions(
    val mode: String,
)

@Serializable
data class Message(
    val role: String,
    val text: String,
)