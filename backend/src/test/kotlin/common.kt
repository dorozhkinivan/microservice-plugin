import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

val yandexGPTResponse = """
{
  "result": {
    "alternatives": [
      {
        "message": {
          "role": "",
          "text": "{\"microservices\": []}"
        }
      }
    ]
  }
}
    """.trimIndent()

fun getHttpClient(engine: HttpClientEngine) = HttpClient(engine) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
}