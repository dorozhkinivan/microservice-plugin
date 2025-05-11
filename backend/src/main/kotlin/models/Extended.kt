package ru.itmo.ivandor.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class GitHubResponse(
    val login: String,
)

@Serializable
data class GitHubAuthResponse(
    @SerialName("access_token")
    val accessToken: String,
)

@Serializable
data class Response(
    val microservices: List<Microservice>,
    var requestId: String = UUID.randomUUID().toString()
)

@Serializable
data class Microservice(
    val name: String,
    val classes: List<String>,
    val description: String,
)

@Serializable
data class Request(
    val classes: List<Class>,
)

@Serializable
data class Class(
    val name: String,
    val methods: List<String>,
)

@Serializable
data class CompleteDto(
    val requestId: String,
    val createdFacades: List<String>,
    val removedFacades: List<String>,
)

@Serializable
data class GptResponse(
    val result: ResultDto,
)
@Serializable
data class ResultDto(
    val alternatives: List<AlternativeDto>,
)
@Serializable
data class AlternativeDto(
    val message: MessageDto,
)
@Serializable
data class MessageDto(
    val role: String,
    val text: String,
)