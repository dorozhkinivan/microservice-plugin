package ru.itmo.ivandor.service

interface JwtService {
    suspend fun generateJwt() : String

    suspend fun validateJwt() : String
}