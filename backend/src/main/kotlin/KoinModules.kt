package ru.itmo.ivandor

import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.dsl.onClose
import ru.itmo.ivandor.dao.AnalyticsDao
import ru.itmo.ivandor.dao.AnalyticsDaoImpl
import ru.itmo.ivandor.dao.createConnectionPool
import ru.itmo.ivandor.service.*

val module = module {
    single {
        HttpClient(CIO) {
            install(HttpTimeout) {
                this.requestTimeoutMillis = 5000
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    single<HikariDataSource> { createConnectionPool() } onClose { it?.close() }
    single<AnalyticsDao> { AnalyticsDaoImpl(get()) }
    single<AnalyticsService> { AnalyticsServiceImpl(get()) }
    single<YandexGPTService> { YandexGPTServiceImpl(get(), get()) }
    single<AuthService> { AuthServiceImpl(get()) }
}