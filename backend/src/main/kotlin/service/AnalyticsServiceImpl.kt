package ru.itmo.ivandor.service

import ru.itmo.ivandor.dao.AnalyticsDao
import ru.itmo.ivandor.models.Class
import ru.itmo.ivandor.models.Microservice
import ru.itmo.ivandor.models.CompleteDto

class AnalyticsServiceImpl(
    private val analyticsDao: AnalyticsDao
) : AnalyticsService {
    override suspend fun saveRequestData(login: String, requestId: String, classes: List<Class>) {
        analyticsDao.saveRequestData(
            login = login,
            requestId = requestId,
            classes = classes,
        )
    }

    override suspend fun saveYandexGptData(requestId: String, microservices: List<Microservice>) {
        analyticsDao.saveYandexGptData(
            requestId = requestId,
            microservices = microservices,
        )
    }

    override suspend fun saveResultData(result: CompleteDto) {
        analyticsDao.saveResultData(
            requestId = result.requestId,
            createdFacades = result.createdFacades.toTypedArray(),
            removedFacades = result.removedFacades.toTypedArray(),
        )
    }
}