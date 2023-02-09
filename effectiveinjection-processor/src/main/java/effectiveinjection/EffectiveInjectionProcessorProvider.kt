package effectiveinjection

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class EffectiveInjectionProcessorProvider : SymbolProcessorProvider {

   override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
       EffectiveInjectionProcessor(
           codeGenerator = environment.codeGenerator,
           logger = environment.logger,
       )
}