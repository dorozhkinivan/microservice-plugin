package ru.itmo.ivandor.service

import kotlinx.serialization.Serializable
import ru.itmo.ivandor.models.Class
import ru.itmo.ivandor.models.Response

interface YandexGPTService {
    suspend fun getMicroservicesModules(classNames: List<@Serializable Class>, login: String) : Response
}