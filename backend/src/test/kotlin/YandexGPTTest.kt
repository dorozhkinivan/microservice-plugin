import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import ru.itmo.ivandor.models.Class
import ru.itmo.ivandor.service.AnalyticsService
import ru.itmo.ivandor.service.YandexGPTServiceImpl

class YandexGPTTest {
@Test
@Disabled
fun `check if analytics data saved`(): Unit = runBlocking {
    val login = "dorozhkinIvan"
    val mockEngine = MockEngine { _ ->
        respond(yandexGPTResponse, headers = headersOf(
            HttpHeaders.ContentType, "application/json")
        )
    }
    val analyticsService = mockk<AnalyticsService>(relaxed = true)
    val classes = listOf(Class("demoClass", listOf("demoMethod")))
    val service = YandexGPTServiceImpl(
        client = getHttpClient(mockEngine),
        analyticsService = analyticsService,
    )
    service.getMicroservicesModules(classes, login)

    coVerify(exactly = 1) { analyticsService.saveRequestData(login, any(), classes) }
    coVerify(exactly = 1) { analyticsService.saveYandexGptData(any(), any()) }
}
}