package effectiveinjection

import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

class EffectiveInjectionProcessor : AbstractProcessor() {

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        processingEnv.messager.printWarning("I am alive")
        val elementsToInject = roundEnv?.getElementsAnnotatedWith(Inject::class.java).orEmpty()
        val classesWithInjects: Map<TypeElement, List<VariableElement>> = elementsToInject
            .filterIsInstance<VariableElement>()
            .groupBy { it.enclosingElement as TypeElement }

        for ((classWithInject, fieldsToInject) in classesWithInjects) {
            val generatedClassName = classWithInject.simpleName.toString() + "Injector"
            val generatedFilePackage = packageOf(classWithInject)
            val paramName = "whereWeInject"

            val injectMethodCode = CodeBlock.builder()

            for (fieldToInject: VariableElement in fieldsToInject) {
                injectMethodCode.addStatement("$paramName.${fieldToInject.simpleName} = new \$T()", fieldToInject.asType())
            }

            val injectMethod = MethodSpec.methodBuilder("inject")
                .addParameter(TypeName.get(classWithInject.asType()), paramName)
                .addCode(injectMethodCode.build())
                .addModifiers(Modifier.STATIC)
                .build()

            val injectorClass = TypeSpec.classBuilder(generatedClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(injectMethod)
                .build()

            val javaFile = JavaFile.builder(generatedFilePackage, injectorClass)
                .build()

            javaFile.writeTo(processingEnv.filer)
        }

//        val main = MethodSpec.methodBuilder("main")
//            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
//            .returns(Void.TYPE)
//            .addParameter(Array<String>::class.java, "args")
//            .addStatement("\$T.out.println(\$S)", System::class.java, "Hello, JavaPoet!")
//            .build()
//
//        val helloWorld = TypeSpec.classBuilder("HelloWorld")
//            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
//            .addMethod(main)
//            .build()
//
//        val javaFile = JavaFile.builder("com.example.helloworld", helloWorld)
//            .build()
//
//        javaFile.writeTo(processingEnv.filer)
        return true
    }

    private fun packageOf(classWithInject: TypeElement): String? {
        val qualifiedName = classWithInject.qualifiedName.toString()
        return if ("." in qualifiedName) qualifiedName.substringBeforeLast(".") else "effectiveinjection"
    }

    override fun getSupportedAnnotationTypes(): Set<String> =
        setOf(Inject::class.qualifiedName!!, AutoWire::class.qualifiedName!!)

    override fun getSupportedSourceVersion(): SourceVersion =
        SourceVersion.latestSupported()
}