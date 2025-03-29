package ru.itmo.ivandor.plugin.dto

data class ResponseDto(
    val microservices: List<MicroserviceDto>
)

data class MicroserviceDto(
    val name: String,
    val description: String,
    val classes: List<String>
)