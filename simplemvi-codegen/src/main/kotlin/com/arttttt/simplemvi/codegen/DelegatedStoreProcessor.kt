package com.arttttt.simplemvi.codegen

import com.arttttt.simplemvi.annotations.DelegatedStore
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

class DelegatedStoreProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(DelegatedStore::class.qualifiedName!!)
        val ret = symbols.filter { !it.validate() }.toList()

        symbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach { it.accept(StoreVisitor(), Unit) }

        return ret
    }

    inner class StoreVisitor : KSVisitorVoid() {

        @OptIn(KspExperimental::class)
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val storeDeclaration = classDeclaration.superTypes
                .map { it.resolve() }
                .firstOrNull {
                    val declaration = it.declaration
                    val qualifiedName = declaration.qualifiedName?.asString()
                    qualifiedName == "com.arttttt.simplemvi.store.Store"
                }

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
            val generatedInterfaceName = "${storeName}IntentHandler"
            val packageName = classDeclaration.packageName.asString()

            val intentGeneric = TypeVariableName("I", intentType)

            val intentHandlerType = ClassName("com.arttttt.simplemvi.actor.delegated", "IntentHandler")
                .parameterizedBy(
                    intentType,
                    stateType,
                    sideEffectType,
                    intentGeneric
                )

            val fileSpec = FileSpec.builder(packageName, generatedInterfaceName)
                .addType(
                    TypeSpec.interfaceBuilder(generatedInterfaceName)
                        .addTypeVariable(intentGeneric)
                        .addSuperinterface(intentHandlerType)
                        .build()
                )
                .build()

            try {
                fileSpec.writeTo(codeGenerator, Dependencies(true, classDeclaration.containingFile!!))
            } catch (e: Exception) {
                logger.error("Error writing file", classDeclaration)
            }
        }
    }
}