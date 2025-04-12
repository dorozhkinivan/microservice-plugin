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
    override val gptToken: String = System.getenv("GPT_TOKEN") ?: throw RuntimeException("No GPT_TOKEN")
    override val oauthClientId: String = System.getenv("OAUTH_CLIENT_ID") ?: throw RuntimeException("OAUTH_CLIENT_ID")
    override val oauthSecret: String = System.getenv("OAUTH_SECRET") ?: throw RuntimeException("No OAUTH_SECRET")
    override val jwtSecret: String = System.getenv("JWT_SECRET") ?: throw RuntimeException("No JWT_SECRET")
    override val ycFolder: String = System.getenv("YC_FOLDER") ?: throw RuntimeException("No YC_FOLDER")
    override val jwtLifetimeMs: Int = System.getenv("JWT_LIFETIME")?.toIntOrNull() ?: 180_000//43_200_000
}