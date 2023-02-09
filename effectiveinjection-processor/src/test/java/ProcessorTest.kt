import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders
import effectiveinjection.EffectiveInjectionProcessorProvider
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

class ProcessorTest {
    @Test
    fun simpleCase() {
        val compilation = KotlinCompilation().apply {
            inheritClassPath = true
            kspWithCompilation = true


            sources = listOf(
                SourceFile.kotlin("CoffeeMaker.kt", """
                    import effectiveinjection.Inject                    

                    class CoffeeMaker {
                        @Inject lateinit var heater: Heater
                        @Inject lateinit var grinder: Grinder
                    }
                       
                    class Heater
                    class Grinder
                """.trimIndent())
            )
            symbolProcessorProviders = listOf(
                EffectiveInjectionProcessorProvider()
            )
        }
        val result = compilation.compile()
        assert(result.exitCode == KotlinCompilation.ExitCode.OK)


        val generated = File(
            compilation.kspSourcesDir,
            "kotlin/CoffeeMakerInjector.kt"
        )
        assertEquals(
            """
                import kotlin.Unit

                public object CoffeeMakerInjector {
                  public fun inject(whereWeInject: CoffeeMaker): Unit {
                    whereWeInject.heater = Heater()
                    whereWeInject.grinder = Grinder()
                  }
                }
            """.trimIndent(),
            generated.readText().trimIndent()
        )
    }
}
