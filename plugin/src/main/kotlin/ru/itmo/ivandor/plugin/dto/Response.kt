package ru.itmo.ivandor.plugin.dto

data class ResponseDto(
    var microservices: List<MicroserviceDto>,
    var requestId: String,
)

data class MicroserviceDto(
    var name: String,
    var description: String,
    var classes: List<String>
)