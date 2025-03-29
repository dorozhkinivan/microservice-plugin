package ru.itmo.ivandor.service

import kotlinx.serialization.Serializable
import ru.itmo.ivandor.models.Class
import ru.itmo.ivandor.models.Respp

interface YandexGPTService {
    suspend fun getMicroservicesModules(classNames: List<@Serializable Class>, contextInfo: Any?) : Respp

    suspend fun getModulesForContext(classNames: List<String>, contextInfo: Any?) : Any
}