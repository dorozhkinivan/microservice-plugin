package ru.itmo.ivandor.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import ru.itmo.ivandor.models.*
import ru.itmo.ivandor.utils.Settings


class YandexGPTServiceImpl(
    private val client: HttpClient,
    private val analyticsService: AnalyticsService,
) : YandexGPTService {
    override suspend fun getMicroservicesModules(classNames: List<Class>, login: String): Response {
        val requestBody = """
            {
                "modelUri": "gpt://${Settings.ycFolder}/yandexgpt/rc",
                "completionOptions": {
                    "stream": false,
                    "temperature": 0.7,
                    "reasoningOptions": {
                        "mode": "DISABLED"
                    }
                },
                "messages": [
                    {
                        "role": "system",
                        "text": "You are an intelligent assistant who helps optimize code in Java and Kotlin. You have been given a list of classes and its methods. Think about which of them should be moved to microservices, creating facade classes at the same time for a smooth transition. Group classes by cohesive business domains. But create only those microservices that you think would be useful for the project architecture. There shouldn't be too many of them, no more than 3."
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

        val response = client.post("https://llm.api.cloud.yandex.net/foundationModels/v1/completion") {
            header("x-folder-id", Settings.ycFolder)
            header("Authorization", "Bearer ${Settings.gptToken}")
            header("Content-Type", "application/json")
            setBody(requestBody)
        }

        if (!response.status.isSuccess())
            throw RuntimeException("Got error from YandexGpt: ${response.bodyAsText()}, ${response.status.value}")


        val text = response.body<GptResponse>().result.alternatives.first().message.text
        val gptResponse = Json.decodeFromString<Response>(text)

//        try {
//            analyticsService.saveRequestData(
//                login = login,
//                requestId = gptResponse.requestId,
//                classes = classNames
//            )
//            analyticsService.saveYandexGptData(
//                requestId = gptResponse.requestId,
//                microservices = gptResponse.microservices,
//            )
//        } catch (e: Throwable){
//            e.printStackTrace()
//        }

        return gptResponse
    }
}
