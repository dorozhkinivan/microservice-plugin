package ru.itmo.ivandor.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.itmo.ivandor.models.*
import ru.itmo.ivandor.utils.SettingsImpl
import java.sql.DriverManager


class YandexGPTServiceImpl(
    private val client: HttpClient,
) : YandexGPTService {
    override suspend fun getMicroservicesModules(classNames: List<Class>, contextInfo: Any?): Respp {
        val reqq = """
            {
                "modelUri": "gpt://${SettingsImpl.ycFolder}/yandexgpt/rc",
                "completionOptions": {
                    "stream": false,
                    "temperature": 0.8,
                    "reasoningOptions": {
                        "mode": "DISABLED"
                    }
                },
                "messages": [
                    {
                        "role": "system",
                        "text": "You are an intelligent assistant who helps optimize code in Java and Kotlin. You have been given a list of classes and its methods. Think about which of them should be moved to microservices, creating facade classes at the same time for a smooth transition. But create only those microservices that you think would be useful for the project architecture. There shouldn't be too many of them. The class names should contain the word 'Facade'."
                    },
                    {
                        "role": "user",
                        "text": "${classNames.joinToString(separator = ", ") { "Class: '${it.name}', methods: [${it.methods.joinToString(separator = ",")}]" }}"
                    }
                ],
  "json_schema": {
    "schema": {
      "type": "object",
      "properties": {
        "microservices": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "name": {
                "type": "string",
                "description": "Microservice facade class name."
              },
              "classes": {
                "type": "array",
                "items": {
                  "type": "string",
                  "description": "Class from project, related to new microservice"
                },
                "description": "Classes array"
              },
              "description": {
                "type": "string",
                "description": "Write the reason why you created this particular class? What methods influenced your decision?"
              }
            },
            "required": ["name", "classes", "description"]
          }
        }
      },
      "required": ["microservices"]
    }
  }

            }
        """.trimIndent()




        val reqqq = Req(
            modelUri = "nda",
            completionOptions = Conf(
                stream = false,
                temperature = 0.3,
                maxTokens = 5000,
                reasoningOptions = ReasoningOptions(
                    mode = "DISABLED"
                )
            ),
            jsonSchema = JsonSchema.default,
            messages = listOf(
                Message(
                    role = "system",
                    text = """
                        Ты умный ассистент, помогаешь оптимизировать код на java и kotlin.Тебе передан список классов. Подумай, какие из них них стоит вынести в микросервисы.
                    """.trimIndent()
                ),
//                    Message(
//                        role = "user",
//                        text = """
//                        Какими науками занимался Альберт Эйнштейн?
//                    """.trimIndent()
//                    ),
//                    Message(
//                        role = "assistant",
//                        text = """
//                       {"text": "Альберт Эйнштейн занимался физикой, а также работал в области математики и философии науки."}
//                    """.trimIndent()
//                    ),
                Message(
                    role = "user",
                    text = """
                        %abob
                    """.trimIndent()
                ),
            )
        )


        val resp = client.post("https://llm.api.cloud.yandex.net/foundationModels/v1/completion") {
            header("x-folder-id", SettingsImpl.ycFolder)
            header("Authorization", "Bearer ${SettingsImpl.gptToken}")
            header("Content-Type", "application/json")
            setBody(reqq)
        }

        if (!resp.status.isSuccess())
        throw RuntimeException("!!! ${resp.bodyAsText()}  | ${resp.status.value}!!!")


        val text = resp.body<DataClass>().result.alternatives.first().message.text

        val c = Json.decodeFromString<Respp>(text)

        return c
    }

    override suspend fun getModulesForContext(classNames: List<String>, contextInfo: Any?): Any {
        TODO("Not yet implemented")
    }
}
