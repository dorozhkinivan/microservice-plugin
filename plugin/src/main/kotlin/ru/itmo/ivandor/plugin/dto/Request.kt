package ru.itmo.ivandor.plugin.dto

data class RequestDto(
    val classes: List<ClassDto>,
)

data class ClassDto(
    val name: String,
    val methods: List<String>,
)