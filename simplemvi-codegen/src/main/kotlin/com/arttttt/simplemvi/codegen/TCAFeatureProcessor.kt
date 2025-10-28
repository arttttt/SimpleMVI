package com.arttttt.simplemvi.codegen

import com.arttttt.simplemvi.annotations.TCAFeature
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate

/**
 * KSP processor for generating TCA (The Composable Architecture) wrappers over SimpleMVI Store
 */
class TCAFeatureProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    private data class StoreGenericTypes(
        val intentDeclaration: KSClassDeclaration,
        val stateDeclaration: KSClassDeclaration,
        val sideEffectDeclaration: KSClassDeclaration,
    )

    private data class StateProperty(
        val name: String,
        val type: KSType,
    )

    private data class SealedTypeInfo(
        val name: String,
        val parameters: List<ConstructorParameter>,
    )

    private data class ConstructorParameter(
        val name: String,
        val type: KSType,
    )

    private companion object {
        const val STORE_FQN = "com.arttttt.simplemvi.store.Store"
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotated = resolver
            .getSymbolsWithAnnotation(TCAFeature::class.qualifiedName!!)
            .toList()

        val (valid, invalid) = annotated.partition { it.validate() }
        valid
            .filterIsInstance<KSClassDeclaration>()
            .forEach(::processStore)

        return invalid
    }

    @OptIn(KspExperimental::class)
    private fun processStore(storeClass: KSClassDeclaration) {
        val storeTypes = extractStoreTypes(storeClass) ?: run {
            logger.error("Cannot extract Store generic types from ${storeClass.qualifiedName?.asString()}", storeClass)
            return
        }

        val storeName = storeClass.simpleName.asString()
        val intentType = storeTypes.intentDeclaration
        val stateType = storeTypes.stateDeclaration
        val sideEffectType = storeTypes.sideEffectDeclaration

        val intentInfos = extractSealedTypeInfo(intentType)
        val sideEffectInfos = extractSealedTypeInfo(sideEffectType)

        val stateProperties = extractStateProperties(stateType)

        val swiftCode = generateSwiftCode(
            storeName = storeName,
            sealedTypeInfos = intentInfos,
            sideEffectInfos = sideEffectInfos,
            stateProperties = stateProperties,
        )

        val fileName = "${storeName}TCAFeature"
        val dependencies = Dependencies(aggregating = false, storeClass.containingFile!!)

        codeGenerator.createNewFile(
            dependencies = dependencies,
            packageName = "",
            fileName = fileName,
            extensionName = "swift",
        ).use { output ->
            output.write(swiftCode.toByteArray())
        }
    }

    private fun extractStoreTypes(storeClass: KSClassDeclaration): StoreGenericTypes? {
        val storeInterface = storeClass.superTypes.firstOrNull { superType ->
            val declaration = superType.resolve().declaration
            declaration.qualifiedName?.asString() == STORE_FQN
        } ?: return null

        val typeArguments = storeInterface.resolve().arguments
        if (typeArguments.size != 3) return null

        val intentDecl = typeArguments[0].type?.resolve()?.declaration as? KSClassDeclaration ?: return null
        val stateDecl = typeArguments[1].type?.resolve()?.declaration as? KSClassDeclaration ?: return null
        val sideEffectDecl = typeArguments[2].type?.resolve()?.declaration as? KSClassDeclaration ?: return null

        return StoreGenericTypes(
            intentDeclaration = intentDecl,
            stateDeclaration = stateDecl,
            sideEffectDeclaration = sideEffectDecl,
        )
    }

    private fun extractSealedTypeInfo(declaration: KSClassDeclaration): List<SealedTypeInfo> {
        return declaration.getSealedSubclasses()
            .map { subclass ->
                val params = subclass.primaryConstructor?.parameters
                    ?.map { param ->
                        ConstructorParameter(
                            name = param.name?.asString() ?: "",
                            type = param.type.resolve(),
                        )
                    }
                    ?: emptyList()

                SealedTypeInfo(
                    name = subclass.simpleName.asString(),
                    parameters = params,
                )
            }
            .toList()
    }

    private fun extractStateProperties(stateDecl: KSClassDeclaration): List<StateProperty> {
        return stateDecl
            .getAllProperties()
            .filter { !it.simpleName.asString().startsWith("_") }
            .map { prop ->
                StateProperty(
                    name = prop.simpleName.asString(),
                    type = prop.type.resolve(),
                )
            }
            .toList()
    }

    // Orchestrates generation by delegating to specialized functions
    private fun generateSwiftCode(
        storeName: String,
        sealedTypeInfos: List<SealedTypeInfo>,
        sideEffectInfos: List<SealedTypeInfo>,
        stateProperties: List<StateProperty>,
    ): String {
        val featureName = storeName.removeSuffix("Store")

        return buildString {
            append(generateHeader())
            append(generateSideEffectHandlerProtocol(storeName, featureName))
            append(generateDefaultSideEffectHandler(storeName, featureName, sideEffectInfos))
            append(generateDependencyRegistrations(storeName))
            append(generateTCAFeature(storeName, featureName, sealedTypeInfos, stateProperties))
            append(generateStateMapper(storeName, featureName, stateProperties))
            append(generateFactory(storeName, featureName, stateProperties))
            append(generateEquatable(featureName, stateProperties))
            append(generateLifecycleToken(storeName, featureName))
        }
    }

    private fun generateHeader(): String {
        return buildString {
            appendLine("// Generated by SimpleMVI KSP TCAFeatureProcessor")
            appendLine("// Do not edit manually")
            appendLine()
            appendLine("import ComposableArchitecture")
            appendLine("import Shared")
            appendLine("import Foundation")
            appendLine()
            appendLine()
        }
    }

    // Generates SideEffect handler protocol
    private fun generateSideEffectHandlerProtocol(
        storeName: String,
        featureName: String,
    ): String {
        return buildString {
            appendLine("// MARK: - SideEffect Handler Protocol")
            appendLine("protocol ${storeName}SideEffectHandler {")
            appendLine("    func handle(_ effect: ${storeName}SideEffect) -> Effect<${featureName}Feature.Action>")
            appendLine("}")
            appendLine()
            appendLine()
        }
    }

    // Generates default SideEffect handler implementation
    private fun generateDefaultSideEffectHandler(
        storeName: String,
        featureName: String,
        sideEffectInfos: List<SealedTypeInfo>,
    ): String {
        return buildString {
            appendLine("// MARK: - Default Handler Implementation")
            appendLine("struct Default${storeName}SideEffectHandler: ${storeName}SideEffectHandler {")
            appendLine("    func handle(_ effect: ${storeName}SideEffect) -> Effect<${featureName}Feature.Action>{")
            appendLine("        switch effect {")
            for (subtype in sideEffectInfos) {
                appendLine("        case is ${storeName}SideEffect${subtype.name}:")
                appendLine("            return .none")
            }
            appendLine("        default:")
            appendLine("            return .none")
            appendLine("        }")
            appendLine("    }")
            appendLine("}")
            appendLine()
            appendLine()
        }
    }

    // Generates TCA dependency registrations (store, handler, lifecycle)
    private fun generateDependencyRegistrations(storeName: String): String {
        return buildString {
            appendLine("// MARK: - TCA Dependency Registration")
            appendLine("extension DependencyValues {")
            appendLine("    var ${storeName.toCamelCase()}: $storeName {")
            appendLine("        get { self[${storeName}Key.self] }")
            appendLine("        set { self[${storeName}Key.self] = newValue }")
            appendLine("    }")
            appendLine("}")
            appendLine()
            appendLine("private struct ${storeName}Key: DependencyKey {")
            appendLine("    static let liveValue: $storeName = {")
            appendLine("        fatalError(\"$storeName dependency not configured. Provide it via withDependencies.\")")
            appendLine("    }()")
            appendLine("}")
            appendLine()
            appendLine("extension DependencyValues {")
            appendLine("    var ${storeName.toCamelCase()}SideEffectHandler: any ${storeName}SideEffectHandler {")
            appendLine("        get { self[${storeName}SideEffectHandlerKey.self] }")
            appendLine("        set { self[${storeName}SideEffectHandlerKey.self] = newValue }")
            appendLine("    }")
            appendLine("}")
            appendLine()
            appendLine("private struct ${storeName}SideEffectHandlerKey: DependencyKey {")
            appendLine("    static let liveValue: any ${storeName}SideEffectHandler = Default${storeName}SideEffectHandler()")
            appendLine("}")
            appendLine()
            appendLine("extension DependencyValues {")
            appendLine("    var ${storeName.toCamelCase()}Lifecycle: _${storeName}Lifecycle {")
            appendLine("        get { self[_${storeName}LifecycleKey.self] }")
            appendLine("        set { self[_${storeName}LifecycleKey.self] = newValue }")
            appendLine("    }")
            appendLine("}")
            appendLine()
            appendLine("private struct _${storeName}LifecycleKey: DependencyKey {")
            appendLine("    static let liveValue: _${storeName}Lifecycle = {")
            appendLine("        fatalError(\"Lifecycle not configured\")")
            appendLine("    }()")
            appendLine("}")
            appendLine()
        }
    }

    // Generates TCA Feature with State, Action, and Reducer
    private fun generateTCAFeature(
        storeName: String,
        featureName: String,
        intentInfos: List<SealedTypeInfo>,
        stateProperties: List<StateProperty>,
    ): String {
        return buildString {
            appendLine("// MARK: - TCA Feature")
            appendLine("@Reducer")
            appendLine("struct ${featureName}Feature {")
            appendLine("    ")
            append(generateFeatureState(stateProperties))
            append(generateFeatureAction(storeName, intentInfos))
            append(generateReducerBody(storeName, intentInfos))
            appendLine("}")
            appendLine()
            appendLine()
        }
    }

    // Generates @ObservableState struct
    private fun generateFeatureState(stateProperties: List<StateProperty>): String {
        return buildString {
            appendLine("    @ObservableState")
            appendLine("    struct State: Equatable {")
            for (prop in stateProperties) {
                val swiftType = prop.type.toSwiftTypeString()
                appendLine("        var ${prop.name}: $swiftType")
            }
            appendLine("    }")
            appendLine()
        }
    }

    // Generates @CasePathable enum Action
    private fun generateFeatureAction(
        storeName: String,
        intentInfos: List<SealedTypeInfo>,
    ): String {
        return buildString {
            appendLine("    @CasePathable")
            appendLine("    enum Action {")
            for (intent in intentInfos) {
                if (intent.parameters.isEmpty()) {
                    appendLine("        case ${intent.name.toCamelCase()}")
                } else {
                    val params = intent.parameters.joinToString(", ") { param ->
                        "${param.name}: ${param.type.toSwiftTypeString()}"
                    }
                    appendLine("        case ${intent.name.toCamelCase()}($params)")
                }
            }
            appendLine()
            appendLine("        case _stateUpdated(${storeName}.State)")
            appendLine("        case _sideEffect(${storeName}SideEffect)")
            appendLine("    }")
            appendLine()
        }
    }

    // Generates Reducer body with dependencies and switch cases
    private fun generateReducerBody(
        storeName: String,
        intentInfos: List<SealedTypeInfo>,
    ): String {
        return buildString {
            appendLine("    @Dependency(\\.${storeName.toCamelCase()}) var store")
            appendLine("    @Dependency(\\.${storeName.toCamelCase()}SideEffectHandler) var sideEffectHandler")
            appendLine("    @Dependency(\\.${storeName.toCamelCase()}Lifecycle) var lifecycle")
            appendLine()
            appendLine("    var body: some ReducerOf<Self> {")
            appendLine("        Reduce { state, action in")
            appendLine("            switch action {")
            for (intent in intentInfos) {
                val caseName = intent.name.toCamelCase()
                if (intent.parameters.isEmpty()) {
                    appendLine("            case .$caseName:")
                    appendLine("                store.accept(intent: ${storeName}Intent${intent.name}())")
                } else {
                    val bindParams = intent.parameters.joinToString(", ") { "let ${it.name}" }
                    val passParams = intent.parameters.joinToString(", ") { "${it.name}: ${it.name}" }
                    appendLine("            case .$caseName($bindParams):")
                    appendLine("                store.accept(intent: ${storeName}Intent${intent.name}($passParams))")
                }
                appendLine("                return .none")
                appendLine("                ")
            }
            appendLine("            case let ._stateUpdated(domain):")
            appendLine("                state.apply(from: domain)")
            appendLine("                return .none")
            appendLine()
            appendLine("            case let ._sideEffect(sideEffect):")
            appendLine("                return sideEffectHandler.handle(sideEffect)")
            appendLine("            }")
            appendLine("        }")
            appendLine("    }")
        }
    }

    // Generates State mapper extension
    private fun generateStateMapper(
        storeName: String,
        featureName: String,
        stateProperties: List<StateProperty>,
    ): String {
        return buildString {
            appendLine("// MARK: - StoreState → Feature.State Mapper")
            appendLine("extension ${featureName}Feature.State {")
            appendLine("    mutating func apply(from domain: ${storeName}.State) {")
            for (prop in stateProperties) {
                val conversion = if (prop.type.declaration.simpleName.asString() in listOf("Int", "Long")) {
                    "Int(domain.${prop.name})"
                } else {
                    "domain.${prop.name}"
                }
                appendLine("        self.${prop.name} = $conversion")
            }
            appendLine("    }")
            appendLine("}")
            appendLine()
        }
    }

    // Generates factory extension with lifecycle binding
    private fun generateFactory(
        storeName: String,
        featureName: String,
        stateProperties: List<StateProperty>,
    ): String {
        return buildString {
            appendLine("// MARK: - Factory")
            appendLine("extension ${featureName}Feature {")
            appendLine("    ")
            appendLine("    static func from(")
            appendLine("        store: ${storeName},")
            appendLine("        withDependencies configureDependencies: @escaping (inout DependencyValues) -> Void = { _ in }")
            appendLine("    ) -> StoreOf<Self> {")
            appendLine("        let lifecycle = _${storeName}Lifecycle(store: store)")
            appendLine()
            appendLine("        let tcaStore = Store(")
            appendLine("            initialState: State(")
            for (prop in stateProperties) {
                val conversion = if (prop.type.declaration.simpleName.asString() in listOf("Int", "Long")) {
                    "Int(store.state.${prop.name})"
                } else {
                    "store.state.${prop.name}"
                }
                appendLine("                ${prop.name}: $conversion,")
            }
            appendLine("            )")
            appendLine("        ) {")
            appendLine("            ${featureName}Feature()")
            appendLine("        } withDependencies: { deps in")
            appendLine("            deps.${storeName.toCamelCase()} = store")
            appendLine("            deps.${storeName.toCamelCase()}Lifecycle = lifecycle")
            appendLine("            configureDependencies(&deps)")
            appendLine("        }")
            appendLine()
            appendLine("        lifecycle.start { action in")
            appendLine("            await tcaStore.send(action)")
            appendLine("        }")
            appendLine()
            appendLine("        return tcaStore")
            appendLine("    }")
            appendLine("}")
            appendLine()
        }
    }

    // Generates Equatable conformance
    private fun generateEquatable(
        featureName: String,
        stateProperties: List<StateProperty>,
    ): String {
        return buildString {
            appendLine("// MARK: - Equatable")
            appendLine("extension ${featureName}Feature.State {")
            appendLine("    static func == (lhs: Self, rhs: Self) -> Bool {")
            for (index in stateProperties.indices) {
                val prop = stateProperties[index]
                val typeName = prop.type.declaration.simpleName.asString()
                val isKotlinType = typeName in listOf(
                    "Array", "MutableList", "ArrayList",
                    "MutableSet", "HashSet", "LinkedHashSet",
                    "MutableMap", "HashMap", "LinkedHashMap",
                )

                val operator = if (isKotlinType) "===" else "=="
                if (index == stateProperties.lastIndex) {
                    appendLine("        return lhs.${prop.name} $operator rhs.${prop.name}")
                } else {
                    appendLine("        guard lhs.${prop.name} $operator rhs.${prop.name} else { return false }")
                }
            }
            appendLine("    }")
            appendLine("}")
            appendLine()
        }
    }

    // Generates lifecycle token class with observers
    private fun generateLifecycleToken(
        storeName: String,
        featureName: String,
    ): String {
        return buildString {
            appendLine("// MARK: - Lifecycle Token")
            appendLine("final class _${storeName}Lifecycle {")
            appendLine("    private let store: $storeName")
            appendLine("    private var observerTask: Task<Void, Never>?")
            appendLine()
            appendLine("    init(store: ${storeName}) {")
            appendLine("        self.store = store")
            appendLine("        store.doInit()")
            appendLine("    }")
            appendLine()
            appendLine("    func start(send: @escaping (${featureName}Feature.Action) async -> Void) {")
            appendLine("        observerTask = Task {")
            appendLine("            await withTaskGroup(of: Void.self) { group in")
            appendLine("                group.addTask {")
            appendLine("                    do {")
            appendLine("                        for try await state in asAsyncThrowingStream(CStateFlow<${storeName}.State>(source: self.store.states)) {")
            appendLine("                            await send(._stateUpdated(state))")
            appendLine("                        }")
            appendLine("                    } catch {}")
            appendLine("                }")
            appendLine()
            appendLine("                group.addTask {")
            appendLine("                    do {")
            appendLine("                        for try await effect in asAsyncThrowingStream(CFlow<${storeName}SideEffect>(source: self.store.sideEffects)) {")
            appendLine("                            await send(._sideEffect(effect))")
            appendLine("                        }")
            appendLine("                    } catch {}")
            appendLine("                }")
            appendLine("            }")
            appendLine("        }")
            appendLine("    }")
            appendLine()
            appendLine("    deinit {")
            appendLine("        observerTask?.cancel()")
            appendLine("        store.destroy()")
            appendLine("    }")
            appendLine("}")
            appendLine()
        }
    }

    private fun String.toCamelCase(): String {
        return replaceFirstChar { it.lowercase() }
    }

    private fun KSType.toSwiftTypeString(): String {
        val decl = declaration
        val simpleName = decl.simpleName.asString()

        val typeArgs = arguments
        val baseType = if (typeArgs.isNotEmpty()) {
            when (simpleName) {
                "List" -> {
                    val arg = typeArgs[0].type?.resolve()?.toSwiftTypeString(wrapPrimitives = false) ?: "Any"
                    "[$arg]"
                }
                "Set" -> {
                    val arg = typeArgs[0].type?.resolve()?.toSwiftTypeString(wrapPrimitives = false) ?: "Any"
                    "Set<$arg>"
                }
                "Map" -> {
                    val keyArg = typeArgs[0].type?.resolve()?.toSwiftTypeString(wrapPrimitives = false) ?: "Any"
                    val valueArg = typeArgs[1].type?.resolve()?.toSwiftTypeString(wrapPrimitives = false) ?: "Any"
                    "Dictionary<$keyArg, $valueArg>"
                }

                "Array" -> {
                    val arg = typeArgs[0].type?.resolve()?.toSwiftTypeString(wrapPrimitives = false) ?: "Any"
                    "KotlinArray<$arg>"
                }
                "MutableList", "ArrayList" -> {
                    val arg = typeArgs[0].type?.resolve()?.toSwiftTypeString(wrapPrimitives = false) ?: "Any"
                    "KotlinMutableArray<$arg>"
                }
                "MutableSet", "HashSet", "LinkedHashSet" -> {
                    val arg = typeArgs[0].type?.resolve()?.toSwiftTypeString(wrapPrimitives = true) ?: "Any"
                    "KotlinMutableSet<$arg>"
                }
                "MutableMap", "HashMap", "LinkedHashMap" -> {
                    val keyArg = typeArgs[0].type?.resolve()?.toSwiftTypeString(wrapPrimitives = true) ?: "Any"
                    val valueArg = typeArgs[1].type?.resolve()?.toSwiftTypeString(wrapPrimitives = true) ?: "Any"
                    "KotlinMutableDictionary<$keyArg, $valueArg>"
                }

                else -> "$simpleName<${typeArgs.mapNotNull { it.type?.resolve()?.toSwiftTypeString() }.joinToString(", ")}>"
            }
        } else {
            primitiveMapping(simpleName, wrapForObjC = false)
        }
        return if (isMarkedNullable) "$baseType?" else baseType
    }

    private fun KSType.toSwiftTypeString(wrapPrimitives: Boolean): String {
        val decl = declaration
        val simpleName = decl.simpleName.asString()

        return primitiveMapping(simpleName, wrapForObjC = wrapPrimitives)
    }

    private fun primitiveMapping(simpleName: String, wrapForObjC: Boolean): String {
        if (wrapForObjC) {
            return when (simpleName) {
                "String" -> "NSString"
                "Int" -> "KotlinInt"
                "Long" -> "KotlinLong"
                "Byte" -> "KotlinByte"
                "Short" -> "KotlinShort"
                "Boolean" -> "KotlinBoolean"
                "Double" -> "KotlinDouble"
                "Float" -> "KotlinFloat"
                "UInt" -> "KotlinUInt"
                "ULong" -> "KotlinULong"
                else -> simpleName
            }
        }

        return when (simpleName) {
            "Int", "Long" -> "Int"
            "Byte" -> "Int8"
            "Short" -> "Int16"
            "UByte" -> "UInt8"
            "UShort" -> "UInt16"
            "UInt" -> "UInt"
            "ULong" -> "UInt64"
            "Char" -> "Character"
            "String" -> "String"
            "Boolean" -> "Bool"
            "Double" -> "Double"
            "Float" -> "Float"
            else -> simpleName
        }
    }
}