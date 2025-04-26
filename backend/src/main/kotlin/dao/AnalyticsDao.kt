package ru.itmo.ivandor.dao

import ru.itmo.ivandor.models.Class
import ru.itmo.ivandor.models.Microservice

interface AnalyticsDao {
    suspend fun saveRequestData(
        login: String,
        requestId: String,
        classes: List<Class>,
    )

    suspend fun saveYandexGptData(
        requestId: String,
        microservices: List<Microservice>
    )

    suspend fun saveResultData(
        requestId: String,
        createdFacades: Array<String>,
        removedFacades: Array<String>,
    )
}