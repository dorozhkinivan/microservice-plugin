package ru.itmo.ivandor.service

import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.itmo.ivandor.models.*

class YandexGPTServiceImpl(
    private val client: HttpClient,
) : YandexGPTService {
    override suspend fun getMicroservicesModules(classNames: List<String>, contextInfo: Any?): Respp {


        val abob = "UserAuthenticationService, UserAuthService, OrderProcessingService, PaymentGatewayService, CustomerNotificationService, InventoryManagementService, ShippingCalculationService, TaxComputationService, ProductCatalogService, TestingService, LegacyDriveService".repeat(100)


        val reqq = """
            {
                "modelUri": "nda",
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
                        "text": "Ты умный ассистент, помогаешь оптимизировать код на java и kotlin.Тебе передан список классов. Подумай, какие из них них стоит вынести в микросервисы. Но создай только те микросервисы, которые по твоему мнению будут полезны для архитектуры проекта."
                    },
                    {
                        "role": "user",
                        "text": "${abob}"
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
                "description": "Имя микросервиса"
              },
              "classes": {
                "type": "array",
                "items": {
                  "type": "string",
                  "description": "Класс, связанный с микросервисом"
                },
                "description": "Массив классов"
              },
              "description": {
                "type": "string",
                "description": "Расскажи подробно, почему был добавлен этот микросервис"
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

        println(Json { encodeDefaults = true; prettyPrint = true; }.encodeToString(reqq))

        val resp = client.post("https://llm.api.cloud.yandex.net/foundationModels/v1/completion") {
            header("x-folder-id", "nda")
            header("Authorization", "Bearer nda")
            header("Content-Type", "application/json")
            setBody(reqq)
        }

        val text = resp.body<DataClass>().result.alternatives.first().message.text

        println(">>> $text")
        val c = Json.decodeFromString<Respp>(text)

        println(c)
        println(c.microservices.size)
        return c
    }

    override suspend fun getModulesForContext(classNames: List<String>, contextInfo: Any?): Any {
        TODO("Not yet implemented")
    }
}