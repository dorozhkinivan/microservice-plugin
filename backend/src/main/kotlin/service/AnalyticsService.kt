package ru.itmo.ivandor.service

import ru.itmo.ivandor.models.Class
import ru.itmo.ivandor.models.CompleteDto
import ru.itmo.ivandor.models.Microservice

interface AnalyticsService {
    suspend fun saveRequestData(
        login: String,
        requestId: String,
        classes: List<Class>
    )

    suspend fun saveYandexGptData(
        requestId: String,
        microservices: List<Microservice>
    )

    suspend fun saveResultData(
        result: CompleteDto
    )
}