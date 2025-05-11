import io.ktor.http.*
import org.junit.jupiter.api.Test

import ru.itmo.ivandor.service.AnalyticsService
import kotlin.test.assertEquals

class ApiTest {
    @Test
    fun testCompleteRouteWithInvalidAuthentication() {
//        val analyticsService: AnalyticsService = Mockito.mock(AnalyticsService::class.java)
//        withTestApplication({
//            // Настройка маршрутов и аутентификации
//            application.setup()
//            // Внедрение зависимости сервиса аналитики
//            any<AnalyticsService>().answers { analyticsService }
//        }) {
//            handleRequest(HttpMethod.Post, "/complete") {
//                addHeader(HttpHeaders.Authorization, "Bearer invalid_token")
//                setBody("""{"data":"example"}""")
//            }.apply {
//                assertEquals(HttpStatusCode.Unauthorized, response.status())
//                Mockito.verify(analyticsService, Mockito.never()).saveResultData(Mockito.any())
//            }
//        }
    }
}