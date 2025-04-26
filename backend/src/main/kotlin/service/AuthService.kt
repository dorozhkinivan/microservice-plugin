package ru.itmo.ivandor.service

interface AuthService {
    suspend fun generateJwt(oauthCode: String) : String
}