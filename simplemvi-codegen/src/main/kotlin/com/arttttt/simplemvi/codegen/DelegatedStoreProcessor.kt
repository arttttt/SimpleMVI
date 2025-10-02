package com.arttttt.simplemvi.codegen

import com.arttttt.simplemvi.annotations.DelegatedStore
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlin.reflect.KClass

class DelegatedStoreProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(DelegatedStore::class.qualifiedName!!)
        val unvalidatedSymbols = symbols.filter { !it.validate() }.toList()

        symbols
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.validate() }
            .forEach { it.accept(StoreVisitor(), Unit) }

        return unvalidatedSymbols
    }

    inner class StoreVisitor : KSVisitorVoid() {

        @OptIn(KspExperimental::class)
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val storeDeclaration = classDeclaration.superTypes
                .map { it.resolve() }
                .firstOrNull { it.declaration.qualifiedName?.asString() == "com.arttttt.simplemvi.store.Store" }

            if (storeDeclaration == null) {
                logger.error("Class annotated with @DelegatedStore must implement the Store interface", classDeclaration)
                return
            }

            val typeArguments = storeDeclaration.arguments
            if (typeArguments.size < 3) {
                logger.error("Store must have at least 3 generic arguments (Intent, State, SideEffect)", classDeclaration)
                return
            }

            val intentType = typeArguments[0].toTypeName()
            val stateType = typeArguments[1].toTypeName()
            val sideEffectType = typeArguments[2].toTypeName()

            val storeName = classDeclaration.simpleName.asString()
            val packageName = classDeclaration.packageName.asString()

            val generatedInterfaceName = "${storeName}IntentHandler"
            val generatedFunctionName = "${storeName.replaceFirstChar(Char::lowercase)}Handler"
            val generatedClassName = ClassName(packageName, generatedInterfaceName)
            val intentGeneric = TypeVariableName("I", bounds = listOf(intentType))

            val interfaceSpec = buildInterfaceSpec(generatedInterfaceName, intentGeneric, intentType, stateType, sideEffectType)
            val functionSpec = buildFunctionSpec(generatedFunctionName, generatedClassName, intentGeneric, intentType, stateType, sideEffectType)

            val fileSpec = FileSpec.builder(packageName, generatedInterfaceName)
                .addType(interfaceSpec)
                .addFunction(functionSpec)
                .build()

            try {
                fileSpec.writeTo(codeGenerator, Dependencies(true, classDeclaration.containingFile!!))
            } catch (e: Exception) {
                logger.error("Error writing file: ${e.message}", classDeclaration)
            }
        }

        private fun buildInterfaceSpec(
            generatedInterfaceName: String,
            intentGeneric: TypeVariableName,
            intentType: TypeName,
            stateType: TypeName,
            sideEffectType: TypeName
        ): TypeSpec {
            val intentHandlerInterface = ClassName("com.arttttt.simplemvi.actor.delegated", "IntentHandler")
            val parameterizedIntentHandler = intentHandlerInterface.parameterizedBy(intentType, stateType, sideEffectType, intentGeneric)

            return TypeSpec.interfaceBuilder(generatedInterfaceName)
                .addTypeVariable(intentGeneric)
                .addSuperinterface(parameterizedIntentHandler)
                .build()
        }

        private fun buildFunctionSpec(
            generatedFunctionName: String,
            generatedClassName: ClassName,
            intentGeneric: TypeVariableName,
            intentType: TypeName,
            stateType: TypeName,
            sideEffectType: TypeName
        ): FunSpec {
            val actorScopeClass = ClassName("com.arttttt.simplemvi.actor", "ActorScope")
            val kClassClassName = KClass::class.asClassName()

            val parameterizedActorScope =
                actorScopeClass.parameterizedBy(intentType, stateType, sideEffectType)

            val blockLambdaType = LambdaTypeName.get(
                receiver = parameterizedActorScope,
                parameters = listOf(ParameterSpec.builder("intent", intentGeneric).build()),
                returnType = UNIT
            )

            val anonymousImpl = TypeSpec.anonymousClassBuilder()
                .addSuperinterface(generatedClassName.parameterizedBy(intentGeneric))
                .addProperty(
                    PropertySpec.builder(
                        "intentClass",
                        kClassClassName.parameterizedBy(intentGeneric)
                    )
                        .addModifiers(KModifier.OVERRIDE)
                        .initializer("%T::class", intentGeneric)
                        .build()
                )
                .addFunction(
                    FunSpec.builder("handle")
                        .addModifiers(KModifier.OVERRIDE)
                        .receiver(parameterizedActorScope)
                        .addParameter("intent", intentGeneric)
                        .addStatement("block(this, intent)")
                        .build()
                )
                .build()

            return FunSpec.builder(generatedFunctionName)
                .addModifiers(KModifier.INLINE)
                .addTypeVariable(intentGeneric.copy(reified = true))
                .addParameter(
                    ParameterSpec.builder("block", blockLambdaType)
                        .addModifiers(KModifier.CROSSINLINE)
                        .build()
                )
                .returns(generatedClassName.parameterizedBy(intentGeneric))
                .addStatement("return %L", anonymousImpl)
                .build()
        }
    }
}