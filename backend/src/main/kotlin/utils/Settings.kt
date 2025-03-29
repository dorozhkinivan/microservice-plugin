package ru.itmo.ivandor.utils

interface Settings {
    val gptToken: String
    val oauthClientId: String
    val oauthSecret: String
    val jwtSecret: String
    val jwtLifetimeMs: Int
    val ycFolder: String
}

object SettingsImpl : Settings {

}