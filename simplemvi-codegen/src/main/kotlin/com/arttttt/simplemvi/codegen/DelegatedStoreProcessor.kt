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

/**
 * KSP Symbol Processor for generating type-safe intent handlers for stores
 *
 * This processor scans for classes annotated with [@DelegatedStore] and generates
 * companion code to simplify intent handler creation. For each annotated store,
 * it generates:
 *
 * 1. **Interface**: A type-safe [IntentHandler] interface specific to the store
 * 2. **Factory function**: An inline function for creating handlers with minimal boilerplate
 *
 * ## Generated Code Structure
 *
 * For a store class `MyStore`, generates:
 * ```kotlin
 * // File: MyStoreIntentHandler.kt
 *
 * interface MyStoreIntentHandler<I : MyStore.Intent> : IntentHandler<MyStore.Intent, MyStore.State, MyStore.SideEffect, I>
 *
 * inline fun <reified I : MyStore.Intent> myStoreIntentHandler(
 *     crossinline block: ActorScope<MyStore.Intent, MyStore.State, MyStore.SideEffect>.(I) -> Unit
 * ): MyStoreIntentHandler<I> {
 *     // Implementation that creates an anonymous handler
 * }
 * ```
 *
 * @param codeGenerator KSP code generator for creating source files
 * @param logger KSP logger for reporting errors and warnings
 *
 */
class DelegatedStoreProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    /**
     * Data class holding the generic type parameters extracted from a Store
     *
     * Stores the [TypeName] representations of the three required generic
     * parameters of a [Store]: Intent, State, and SideEffect.
     *
     * @property intent The Intent type parameter
     * @property state The State type parameter
     * @property sideEffect The SideEffect type parameter
     */
    private data class StoreTypes(
        val intent: TypeName,
        val state: TypeName,
        val sideEffect: TypeName
    )

    /**
     * Context object containing all information needed for code generation
     *
     * Encapsulates the names, types, and other metadata extracted from
     * the annotated store class that will be used to generate the
     * intent handler interface and factory function.
     *
     * @property pkg Package name where generated code will be placed
     * @property storeName Simple name of the store class
     * @property ifaceName Name for the generated interface
     * @property funName Name for the generated factory function
     * @property ifaceClass [ClassName] reference to the generated interface
     * @property intentGeneric Type variable for the specific intent type parameter
     * @property types The extracted Intent, State, and SideEffect types
     */
    private data class GenCtx(
        val pkg: String,
        val storeName: String,
        val ifaceName: String,
        val funName: String,
        val ifaceClass: ClassName,
        val intentGeneric: TypeVariableName,
        val types: StoreTypes
    )

    /**
     * Visitor for processing [Store] class declarations
     *
     * Visits classes annotated with [DelegatedStore] and generates
     * the corresponding intent handler interface and factory function.
     *
     * Uses KotlinPoet to build the generated code structure.
     */
    inner class StoreVisitor : KSVisitorVoid() {

        /**
         * Processes a class declaration to generate intent handler code
         *
         * This method:
         * 1. Extracts generic types from the Store implementation
         * 2. Computes names for generated code artifacts
         * 3. Builds the interface specification
         * 4. Builds the factory function specification
         * 5. Writes the generated file
         *
         * @param classDeclaration The store class to process
         * @param data Unused visitor data parameter
         */
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

            val initIfaceName = "${storeName}InitHandler"
            val initFunName = storeName.replaceFirstChar(Char::lowercase) + "InitHandler"

            val initFile = FileSpec.builder(pkg, initIfaceName)
                .addType(buildInitHandlerInterfaceSpec(initIfaceName, types, classDeclaration.containingFile!!))
                .addFunction(buildInitHandlerFactoryFunctionSpec(pkg, initIfaceName, initFunName, types, classDeclaration.containingFile!!))
                .build()

            runCatching {
                initFile.writeTo(
                    codeGenerator,
                    Dependencies(aggregating = false, classDeclaration.containingFile!!)
                )
            }.onFailure { e ->
                logger.error("Error writing InitHandler file: ${e.message}", classDeclaration)
            }

            val destroyIfaceName = "${storeName}DestroyHandler"
            val destroyFunName = storeName.replaceFirstChar(Char::lowercase) + "DestroyHandler"

            val destroyFile = FileSpec.builder(pkg, destroyIfaceName)
                .addType(buildDestroyHandlerInterfaceSpec(destroyIfaceName, types, classDeclaration.containingFile!!))
                .addFunction(buildDestroyHandlerFactoryFunctionSpec(pkg, destroyIfaceName, destroyFunName, types, classDeclaration.containingFile!!))
                .build()

            runCatching {
                destroyFile.writeTo(
                    codeGenerator,
                    Dependencies(aggregating = false, classDeclaration.containingFile!!)
                )
            }.onFailure { e ->
                logger.error("Error writing DestroyHandler file: ${e.message}", classDeclaration)
            }
        }

        /**
         * Builds the [IntentHandler] interface specification
         *
         * Generates an interface that extends [IntentHandler] with the store's
         * specific generic types. The interface includes a generic type parameter
         * bounded by the store's Intent type.
         *
         * Example output:
         * ```kotlin
         * interface MyStoreIntentHandler<I : MyStore.Intent> : IntentHandler<MyStore.Intent, MyStore.State, MyStore.SideEffect, I>
         * ```
         *
         * @param ctx Code generation context with type information
         * @param ksFile Source file for tracking code generation origins
         * @return [TypeSpec] for the generated interface
         */
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

        /**
         * Builds the factory function specification
         *
         * Generates an inline function with a reified type parameter that creates
         * instances of the intent handler interface. The function takes a lambda
         * that will be invoked in the context of [ActorScope] when the handler
         * processes an intent.
         *
         * Example output:
         * ```kotlin
         * inline fun <reified I : MyStore.Intent> myStoreIntentHandler(
         *     crossinline block: ActorScope<MyStore.Intent, MyStore.State, MyStore.SideEffect>.(I) -> Unit
         * ): MyStoreIntentHandler<I> {
         *     return object : MyStoreIntentHandler<I> {
         *         override val intentClass: KClass<I> = I::class
         *         override fun ActorScope<...>.handle(intent: I) {
         *             block(this, intent)
         *         }
         *     }
         * }
         * ```
         *
         * @param ctx Code generation context with type information
         * @param ksFile Source file for tracking code generation origins
         * @return [FunSpec] for the generated factory function
         */
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

        /**
         * Builds InitHandler interface specification
         */
        @OptIn(KspExperimental::class)
        private fun buildInitHandlerInterfaceSpec(
            ifaceName: String,
            types: StoreTypes,
            ksFile: KSFile
        ): TypeSpec {
            val initHandlerClass = classNameOf(INIT_HANDLER_FQN)

            return TypeSpec.interfaceBuilder(ifaceName)
                .addSuperinterface(
                    initHandlerClass.parameterizedBy(
                        types.intent,
                        types.state,
                        types.sideEffect,
                    )
                )
                .addOriginatingKSFile(ksFile)
                .build()
        }

        /**
         * Builds InitHandler factory function specification
         */
        @OptIn(KspExperimental::class)
        private fun buildInitHandlerFactoryFunctionSpec(
            pkg: String,
            ifaceName: String,
            funName: String,
            types: StoreTypes,
            ksFile: KSFile
        ): FunSpec {
            val actorScope = classNameOf(ACTOR_SCOPE_FQN).parameterizedBy(
                types.intent,
                types.state,
                types.sideEffect,
            )

            val blockLambda = LambdaTypeName.get(
                receiver = actorScope,
                returnType = UNIT,
            )

            val ifaceClass = ClassName(pkg, ifaceName)

            val anonymousImpl = TypeSpec.anonymousClassBuilder()
                .addSuperinterface(ifaceClass)
                .addFunction(
                    FunSpec.builder("onInit")
                        .addModifiers(KModifier.OVERRIDE)
                        .receiver(actorScope)
                        .addStatement("block(this)")
                        .build()
                )
                .build()

            return FunSpec.builder(funName)
                .addModifiers(KModifier.INLINE)
                .addParameter(
                    ParameterSpec.builder("block", blockLambda)
                        .addModifiers(KModifier.CROSSINLINE)
                        .build()
                )
                .returns(ifaceClass)
                .addStatement("return %L", anonymousImpl)
                .addOriginatingKSFile(ksFile)
                .build()
        }

        /**
         * Builds DestroyHandler interface specification
         */
        @OptIn(KspExperimental::class)
        private fun buildDestroyHandlerInterfaceSpec(
            ifaceName: String,
            types: StoreTypes,
            ksFile: KSFile
        ): TypeSpec {
            val destroyHandlerClass = classNameOf(DESTROY_HANDLER_FQN)

            return TypeSpec.interfaceBuilder(ifaceName)
                .addSuperinterface(
                    destroyHandlerClass.parameterizedBy(
                        types.intent,
                        types.state,
                        types.sideEffect,
                    )
                )
                .addOriginatingKSFile(ksFile)
                .build()
        }

        /**
         * Builds DestroyHandler factory function specification
         */
        @OptIn(KspExperimental::class)
        private fun buildDestroyHandlerFactoryFunctionSpec(
            pkg: String,
            ifaceName: String,
            funName: String,
            types: StoreTypes,
            ksFile: KSFile
        ): FunSpec {
            val actorScope = classNameOf(ACTOR_SCOPE_FQN).parameterizedBy(
                types.intent,
                types.state,
                types.sideEffect,
            )

            val blockLambda = LambdaTypeName.get(
                receiver = actorScope,
                returnType = UNIT,
            )

            val ifaceClass = ClassName(pkg, ifaceName)

            val anonymousImpl = TypeSpec.anonymousClassBuilder()
                .addSuperinterface(ifaceClass)
                .addFunction(
                    FunSpec.builder("onDestroy")
                        .addModifiers(KModifier.OVERRIDE)
                        .receiver(actorScope)
                        .addStatement("block(this)")
                        .build()
                )
                .build()

            return FunSpec.builder(funName)
                .addModifiers(KModifier.INLINE)
                .addParameter(
                    ParameterSpec.builder("block", blockLambda)
                        .addModifiers(KModifier.CROSSINLINE)
                        .build()
                )
                .returns(ifaceClass)
                .addStatement("return %L", anonymousImpl)
                .addOriginatingKSFile(ksFile)
                .build()
        }
    }

    /**
     * Fully qualified names of SimpleMVI types used in code generation
     */
    private companion object {
        const val STORE_FQN = "com.arttttt.simplemvi.store.Store"
        const val INTENT_HANDLER_FQN = "com.arttttt.simplemvi.actor.delegated.IntentHandler"
        const val ACTOR_SCOPE_FQN = "com.arttttt.simplemvi.actor.ActorScope"
        const val INIT_HANDLER_FQN = "com.arttttt.simplemvi.actor.delegated.InitHandler"
        const val DESTROY_HANDLER_FQN = "com.arttttt.simplemvi.actor.delegated.DestroyHandler"
    }

    /**
     * Main processing method called by KSP
     *
     * Finds all symbols (classes) annotated with [DelegatedStore],
     * validates them, and triggers code generation via the [StoreVisitor].
     *
     * @param resolver KSP resolver for looking up symbols in the compilation
     * @return List of symbols that couldn't be validated (for deferred processing)
     */
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

    /**
     * Creates a [ClassName] from a fully qualified name string
     *
     * Splits the FQN into package and simple name components
     * to construct a KotlinPoet [ClassName].
     *
     * @param fqn Fully qualified name (e.g., "com.example.MyClass")
     * @return [ClassName] for use in code generation
     */
    private fun classNameOf(fqn: String): ClassName {
        val pkg = fqn.substringBeforeLast('.')
        val name = fqn.substringAfterLast('.')
        return ClassName(pkg, name)
    }

    /**
     * Extracts and validates Store generic type parameters
     *
     * Validates that the class:
     * 1. Implements the [Store] interface
     * 2. Has at least 3 generic type arguments (Intent, State, SideEffect)
     *
     * Logs errors and returns null if validation fails.
     *
     * @receiver The class declaration to extract types from
     * @return [StoreTypes] containing the extracted types, or null if validation fails
     */
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
