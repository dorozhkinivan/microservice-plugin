package ru.itmo.ivandor.utils

interface Settings {
    val token: String
}

data class SettingsImpl(
    override val token: String = System.getenv("GPT_TOKEN") ?: throw RuntimeException("Token not provided")
) : Settings