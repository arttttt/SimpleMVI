package com.arttttt.simplemvi.codegen

import com.arttttt.simplemvi.annotations.DelegatedStore
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlin.reflect.KClass

class DelegatedStoreProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private data class StoreTypes(
        val intent: TypeName,
        val state: TypeName,
        val sideEffect: TypeName
    )

    private data class GenCtx(
        val pkg: String,
        val storeName: String,
        val ifaceName: String,
        val funName: String,
        val ifaceClass: ClassName,
        val intentGeneric: TypeVariableName,
        val types: StoreTypes
    )

    inner class StoreVisitor : KSVisitorVoid() {

        @OptIn(KspExperimental::class)
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val types = classDeclaration.requireStoreTypesOrReport() ?: return

            val storeName = classDeclaration.simpleName.asString()
            val pkg = classDeclaration.packageName.asString()

            val ifaceName = "${storeName}IntentHandler"
            val funName = storeName.replaceFirstChar(Char::lowercase) + "IntentHandler"
            val ifaceClass = ClassName(pkg, ifaceName)

            val intentGeneric = TypeVariableName(
                "I",
                bounds = listOf(types.intent)
            )

            val ctx = GenCtx(
                pkg = pkg,
                storeName = storeName,
                ifaceName = ifaceName,
                funName = funName,
                ifaceClass = ifaceClass,
                intentGeneric = intentGeneric,
                types = types
            )

            val file = FileSpec.builder(pkg, ifaceName)
                .addType(buildInterfaceSpec(ctx, classDeclaration.containingFile!!))
                .addFunction(buildFactoryFunctionSpec(ctx, classDeclaration.containingFile!!))
                .build()

            runCatching {
                file.writeTo(
                    codeGenerator,
                    Dependencies(aggregating = false, classDeclaration.containingFile!!)
                )
            }.onFailure { e ->
                logger.error("Error writing file: ${e.message}", classDeclaration)
            }
        }

        private fun buildInterfaceSpec(ctx: GenCtx, ksFile: KSFile): TypeSpec {
            return TypeSpec.interfaceBuilder(ctx.ifaceName)
                .addTypeVariable(ctx.intentGeneric)
                .addSuperinterface(
                    classNameOf(INTENT_HANDLER_FQN)
                        .parameterizedBy(ctx.types.intent, ctx.types.state, ctx.types.sideEffect, ctx.intentGeneric)
                )
                .addOriginatingKSFile(ksFile)
                .build()
        }

        private fun buildFactoryFunctionSpec(ctx: GenCtx, ksFile: KSFile): FunSpec {
            val actorScope = classNameOf(ACTOR_SCOPE_FQN)
                .parameterizedBy(ctx.types.intent, ctx.types.state, ctx.types.sideEffect)

            val blockLambda = LambdaTypeName.get(
                receiver = actorScope,
                parameters = listOf(ParameterSpec.builder("intent", ctx.intentGeneric).build()),
                returnType = UNIT
            )

            val kClassType = KClass::class.asClassName().parameterizedBy(ctx.intentGeneric)

            val anonymousImpl = TypeSpec.anonymousClassBuilder()
                .addSuperinterface(ctx.ifaceClass.parameterizedBy(ctx.intentGeneric))
                .addProperty(
                    PropertySpec.builder("intentClass", kClassType)
                        .addModifiers(KModifier.OVERRIDE)
                        .initializer("%T::class", ctx.intentGeneric)
                        .build()
                )
                .addFunction(
                    FunSpec.builder("handle")
                        .addModifiers(KModifier.OVERRIDE)
                        .receiver(actorScope)
                        .addParameter("intent", ctx.intentGeneric)
                        .addStatement("block(this, intent)")
                        .build()
                )
                .build()

            return FunSpec.builder(ctx.funName)
                .addModifiers(KModifier.INLINE)
                .addTypeVariable(ctx.intentGeneric.copy(reified = true))
                .addParameter(
                    ParameterSpec.builder("block", blockLambda)
                        .addModifiers(KModifier.CROSSINLINE)
                        .build()
                )
                .returns(ctx.ifaceClass.parameterizedBy(ctx.intentGeneric))
                .addStatement("return %L", anonymousImpl)
                .addOriginatingKSFile(ksFile)
                .build()
        }
    }

    private companion object {
        const val STORE_FQN = "com.arttttt.simplemvi.store.Store"
        const val INTENT_HANDLER_FQN = "com.arttttt.simplemvi.actor.delegated.IntentHandler"
        const val ACTOR_SCOPE_FQN = "com.arttttt.simplemvi.actor.ActorScope"
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotated = resolver
            .getSymbolsWithAnnotation(DelegatedStore::class.qualifiedName!!)
            .toList()

        val (valid, invalid) = annotated.partition { it.validate() }
        valid
            .filterIsInstance<KSClassDeclaration>()
            .forEach { it.accept(StoreVisitor(), Unit) }

        return invalid
    }

    private fun classNameOf(fqn: String): ClassName {
        val pkg = fqn.substringBeforeLast('.')
        val name = fqn.substringAfterLast('.')
        return ClassName(pkg, name)
    }

    private fun KSClassDeclaration.requireStoreTypesOrReport(): StoreTypes? {
        val storeDecl = superTypes
            .map { it.resolve() }
            .firstOrNull { it.declaration.qualifiedName?.asString() == STORE_FQN }
            ?: return logger.error("Class annotated with @DelegatedStore must implement Store", this).let { null }

        val args = storeDecl.arguments
        if (args.size < 3) {
            logger.error("Store must have at least 3 generic arguments (Intent, State, SideEffect)", this)
            return null
        }

        return StoreTypes(
            intent = args[0].toTypeName(),
            state = args[1].toTypeName(),
            sideEffect = args[2].toTypeName()
        )
    }
}
