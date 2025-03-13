package ru.itmo.ivandor.service

import ru.itmo.ivandor.models.Respp

interface YandexGPTService {
    suspend fun getMicroservicesModules(classNames: List<String>, contextInfo: Any?) : Respp

    suspend fun getModulesForContext(classNames: List<String>, contextInfo: Any?) : Any
}