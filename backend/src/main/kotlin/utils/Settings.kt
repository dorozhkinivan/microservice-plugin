package ru.itmo.ivandor.utils

object Settings {
    val gptToken: String = System.getenv("GPT_TOKEN") ?: throw RuntimeException("No GPT_TOKEN")
    val oauthClientId: String = System.getenv("OAUTH_CLIENT_ID") ?: throw RuntimeException("OAUTH_CLIENT_ID")
    val oauthSecret: String = System.getenv("OAUTH_SECRET") ?: throw RuntimeException("No OAUTH_SECRET")
    val jwtSecret: String = System.getenv("JWT_SECRET") ?: throw RuntimeException("No JWT_SECRET")
    val ycFolder: String = System.getenv("YC_FOLDER") ?: throw RuntimeException("No YC_FOLDER")
    val jwtLifetimeMs: Int = System.getenv("JWT_LIFETIME")?.toIntOrNull() ?: (15 * 60_000)//43_200_000
}