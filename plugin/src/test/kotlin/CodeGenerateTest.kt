import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import junit.framework.TestCase
import ru.itmo.ivandor.plugin.service.code_modify.generateNewKotlinClass

class CodeGenerateTest : LightPlatformCodeInsightTestCase() {
    override fun getTestDataPath() = "src/test"
    fun `test generate Java code facade`(){
        configureByFile("/data/ExampleService.java")
        val psiClass = JavaPsiFacade.getInstance(project).findClass("ExampleService", GlobalSearchScope.allScope(project))!!
        val resultCode =
            ru.itmo.ivandor.plugin.service.code_modify.generateNewJavaClass(project, "ExampleFacade", listOf(psiClass))

        val expected = """
        class ExampleFacade {
            private ExampleService exampleService;
            private boolean useMicroservice = false;
        
            public void demo() {
                if (!useMicroservice)
                    return exampleService.demo();
                throw new UnsupportedOperationException();
            }
        }
        """.trimIndent()

        TestCase.assertEquals(expected, resultCode)
    }

    fun `test generate Kotlin code facade`(){
        configureByFile("/data/ExampleService.java")
        val psiClass = JavaPsiFacade.getInstance(project).findClass("ExampleService", GlobalSearchScope.allScope(project))!!
        val resultCode = generateNewKotlinClass(project, "ExampleFacade", listOf(psiClass))

        val expected = """
        class ExampleFacade {
            private val useMicroservice: Boolean = false
            private val exampleService = ExampleService()
        
            fun demo() {
                if (!useMicroservice)
                    exampleService.demo()
                else
                    throw NotImplementedError()
            }
        }
        """.trimIndent()

        TestCase.assertEquals(expected, resultCode)
    }
}