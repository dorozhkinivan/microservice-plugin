package ru.itmo.ivandor.dao

import com.zaxxer.hikari.HikariDataSource
import ru.itmo.ivandor.models.Class
import ru.itmo.ivandor.models.Microservice
import java.util.*

class AnalyticsDaoImpl(
    private val hc: HikariDataSource
) : AnalyticsDao{
    // sorry, JDBC not supporting such datatypes
    private fun List<String>.toClickhouseFormat() : String = this.joinToString(prefix = "[", postfix = "]") { "'$it'" }
    private fun List<List<String>>.toClickhouseNestedFormat() : String = this.joinToString(prefix = "[", postfix = "]") {
        it.joinToString(prefix = "[", postfix = "]") { "'$it'" }
    }

    override suspend fun saveRequestData(
        login: String,
        requestId: String,
        classes: List<Class>,
    ) {
        hc.useConnection { conn ->
            val classesString = classes.map { it.name }.toClickhouseFormat()
            val methodsString = classes.map { it.methods }.toClickhouseNestedFormat()
            val insertRequestQuery = """
            INSERT INTO request (request_id, user_login, class_methods.class, class_methods.method) VALUES (?, ?, ?, ?);
        """
            conn.prepareStatement(insertRequestQuery).use { preparedStatement ->
                preparedStatement.setObject(1, requestId.toString())
                preparedStatement.setString(2, login)
                preparedStatement.setObject(3, classesString)
                preparedStatement.setObject(4, methodsString)
                preparedStatement.execute()
            }
        }
    }

    override suspend fun saveYandexGptData(
        requestId: String,
        microservices: List<Microservice>,
    ) {
        hc.useConnection { conn ->
            conn.prepareStatement("INSERT INTO response (request_id, facade_classes.facade, facade_classes.description, facade_classes.facade_class) VALUES (?, ?, ?, ?)").use { ps ->
                ps.setObject(1, requestId.toString())
                ps.setObject(2, microservices.map { it.name }.toClickhouseFormat())
                ps.setObject(3, microservices.map { it.description }.toClickhouseFormat())
                ps.setObject(4, microservices.map { it.classes }.toClickhouseNestedFormat())
                ps.execute()
            }
        }
    }

    override suspend fun saveResultData(
        requestId: String,
        createdFacades: Array<String>,
        removedFacades: Array<String>,
    ) {
        hc.useConnection { conn ->
            conn.prepareStatement("INSERT INTO result (request_id, created_facades, removed_facades) VALUES (?, ?, ?)").use { ps ->
                ps.setObject(1, requestId.toString())
                ps.setArray(2, conn.createArrayOf("String", createdFacades))
                ps.setArray(3, conn.createArrayOf("String", removedFacades))
                ps.execute()
            }
        }
    }

}