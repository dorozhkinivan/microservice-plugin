package ru.itmo.ivandor.dao

import com.clickhouse.jdbc.DataSourceImpl
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import ru.itmo.ivandor.logger
import java.sql.Connection
import java.util.Properties


fun <T> HikariDataSource.useConnection(block: (conn: Connection) -> T) = this.connection.use { conn -> block(conn) }

fun createConnectionPool() : HikariDataSource {
    val url = "jdbc:ch://${System.getenv("DB_HOST")}:8443?jdbc_ignore_unsupported_values=true&socket_timeout=10&ssl=true&sslrootcert=/app/RootCA.crt&sslmode=strict"
    val properties =  Properties();
    properties["user"] = "admin"
    properties["password"] = System.getenv("DB_PASS")
    properties["database"] = "analytics"

    val poolConfig = HikariConfig()
    poolConfig.connectionTimeout = 5000L
    poolConfig.maximumPoolSize = 5
    poolConfig.maxLifetime = 300000L
    poolConfig.dataSource = DataSourceImpl(url, properties)

    return HikariDataSource(poolConfig)
}

fun warmUp(cp: HikariDataSource){
    cp.connection.use { conn ->
        val rs = conn.createStatement().executeQuery("SELECT version()")
        if (rs.next()) {
            logger.info("Clickhouse version: ${rs.getString(1)}")
        }
    }
    logger.info("Hikari warmed up")
}