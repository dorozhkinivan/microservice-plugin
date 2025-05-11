package ru.itmo.ivandor.utils

object Settings {
    val gptToken: String = System.getenv("GPT_TOKEN").orEmpty()
    val oauthClientId: String = System.getenv("OAUTH_CLIENT_ID").orEmpty()
    val oauthSecret: String = System.getenv("OAUTH_SECRET").orEmpty()
    val jwtSecret: String = System.getenv("JWT_SECRET").orEmpty()
    val ycFolder: String = System.getenv("YC_FOLDER").orEmpty()
    val jwtLifetimeMs: Int = System.getenv("JWT_LIFETIME")?.toIntOrNull() ?: (15 * 60_000)
}