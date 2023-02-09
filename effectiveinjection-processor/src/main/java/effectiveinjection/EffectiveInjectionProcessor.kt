package effectiveinjection

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

class EffectiveInjectionProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val elementsToInject = resolver.getSymbolsWithAnnotation(Inject::class.qualifiedName.toString())
        val classesWithInjects = elementsToInject
            .filterIsInstance<KSPropertyDeclaration>()
            .groupBy { it.parent as KSClassDeclaration }

        for ((classWithInject: KSClassDeclaration, fieldsToInject) in classesWithInjects) {
            val generatedClassName = classWithInject.simpleName.getShortName() + "Injector"
            val generatedFilePackage = classWithInject.qualifiedName?.getQualifier().orEmpty()
            val paramName = "whereWeInject"

            val injectMethodCode = CodeBlock.builder()

            for (fieldToInject in fieldsToInject) {
                injectMethodCode.addStatement("$paramName.${fieldToInject.simpleName.getShortName()} = %T()", fieldToInject.type.toTypeName())
            }

            val injectMethod = FunSpec.builder("inject")
                .addParameter(paramName, classWithInject.asType(emptyList()).toTypeName())
                .addCode(injectMethodCode.build())
                .build()

            val injectorObject = TypeSpec.objectBuilder(generatedClassName)
                .addFunction(injectMethod)
                .build()

            val javaFile = FileSpec.builder(generatedFilePackage, generatedClassName)
                .addType(injectorObject)
                .build()

            val dependencies = Dependencies(aggregating = false, classWithInject.containingFile!!)
            val file = codeGenerator.createNewFile(dependencies, javaFile.packageName, javaFile.name)
            OutputStreamWriter(file, StandardCharsets.UTF_8)
                .use(javaFile::writeTo)
        }

        return emptyList()
    }
}