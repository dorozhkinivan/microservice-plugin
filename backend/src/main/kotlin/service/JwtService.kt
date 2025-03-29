package ru.itmo.ivandor.service

interface JwtService {
    suspend fun generateJwt(oauthCode: String) : String
}