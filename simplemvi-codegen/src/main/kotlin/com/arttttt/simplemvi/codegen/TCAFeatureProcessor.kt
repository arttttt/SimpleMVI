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
            stateProperties = stateProperties
        )

        val fileName = "${storeName}TCAFeature"
        val dependencies = Dependencies(aggregating = false, storeClass.containingFile!!)

        codeGenerator.createNewFile(
            dependencies = dependencies,
            packageName = "",
            fileName = fileName,
            extensionName = "swift"
        ).use { output ->
            output.write(swiftCode.toByteArray())
        }
    }

    /**
     * Extracts Store<Intent, State, SideEffect> generic types
     */
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
            sideEffectDeclaration = sideEffectDecl
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

    /**
     * Extracts properties from data class State
     */
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

    /**
     * Generates Swift code
     *
     * todo: remove bridge reducer
     * todo: make separated functions for different pieces
     */
    private fun generateSwiftCode(
        storeName: String,
        sealedTypeInfos: List<SealedTypeInfo>,
        sideEffectInfos: List<SealedTypeInfo>,
        stateProperties: List<StateProperty>
    ): String {
        val featureName = storeName.removeSuffix("Store")

        return buildString {
            appendLine("// Generated by SimpleMVI KSP TCAFeatureProcessor")
            appendLine("// Do not edit manually")
            appendLine()
            appendLine("import ComposableArchitecture")
            appendLine("import Shared")
            appendLine("import Foundation")
            appendLine()
            appendLine()

            // SideEffect Handler Protocol
            appendLine("// MARK: - SideEffect Handler Protocol")
            appendLine("protocol ${storeName}SideEffectHandler {")
            appendLine("    func handle(_ effect: ${storeName}SideEffect) -> Effect<${featureName}Feature.Action>")
            appendLine("}")
            appendLine()
            appendLine()

            // Default Handler
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

            // TCA Dependency Registration
            appendLine("// MARK: - TCA Dependency Registration")
            appendLine("extension DependencyValues {")
            appendLine("    var ${storeName.toCamelCase()}: ${storeName} {")
            appendLine("        get { self[${storeName}Key.self] }")
            appendLine("        set { self[${storeName}Key.self] = newValue }")
            appendLine("    }")
            appendLine("}")
            appendLine()
            appendLine("private struct ${storeName}Key: DependencyKey {")
            appendLine("    static let liveValue: ${storeName} = {")
            appendLine("        fatalError(\"${storeName} dependency not configured. Provide it via withDependencies.\")")
            appendLine("    }()")
            appendLine("}")
            appendLine()
            appendLine("extension DependencyValues {")
            appendLine("    var ${storeName.toCamelCase()}SideEffectHandler: any ${storeName}SideEffectHandler {")
            appendLine("        get { self[${storeName}SideEffectHandlerKey.self] }")
            appendLine("        set { self[${storeName}SideEffectHandlerKey.self] = newValue }")
            appendLine("    }")
            appendLine("}")
            appendLine("private struct ${storeName}SideEffectHandlerKey: DependencyKey {")
            appendLine("    static let liveValue: any ${storeName}SideEffectHandler = Default${storeName}SideEffectHandler()")
            appendLine("}")
            appendLine()
            appendLine()

            // TCA Feature
            appendLine("// MARK: - TCA Feature")
            appendLine("@Reducer")
            appendLine("struct ${featureName}Feature {")
            appendLine("    ")
            appendLine("    @ObservableState")
            appendLine("    struct State: Equatable {")
            for (prop in stateProperties) {
                val swiftType = prop.type.toSwiftTypeString()
                appendLine("        var ${prop.name}: $swiftType")
            }
            appendLine("        var _bridge = ${storeName}BridgeReducer.State()")
            appendLine("        var _lifecycle: _${storeName}Lifecycle?")
            appendLine("    }")
            appendLine()
            appendLine("    @CasePathable")
            appendLine("    enum Action {")
            for (intent in sealedTypeInfos) {
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
            appendLine("        case _bridge(${storeName}BridgeReducer.Action)")
            appendLine("        case _setLifecycle(_${storeName}Lifecycle?)")
            appendLine("    }")
            appendLine()
            appendLine("    @Dependency(\\.${storeName.toCamelCase()}) var store")
            appendLine("    @Dependency(\\.${storeName.toCamelCase()}SideEffectHandler) var sideEffectHandler")
            appendLine()
            appendLine("    var body: some ReducerOf<Self> {")
            appendLine("        Scope(state: \\._bridge, action: \\._bridge) {")
            appendLine("            ${storeName}BridgeReducer()")
            appendLine("        }")
            appendLine("        ")
            appendLine("        Reduce { state, action in")
            appendLine("            switch action {")
            for (intent in sealedTypeInfos) {
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
            appendLine("            case let ._bridge(.stateUpdated(domain)):")
            appendLine("              state.apply(from: domain)")
            appendLine("              return .none")
            appendLine()
            appendLine("            case let ._bridge(.sideEffect(sideEffect)):")
            appendLine("              return sideEffectHandler.handle(sideEffect.wrapped)")
            appendLine()
            appendLine("            case ._bridge(.startObserving), ._bridge(.stopObserving):")
            appendLine("              return .none")
            appendLine("            case let ._setLifecycle(lifecycle):")
            appendLine("                state._lifecycle = lifecycle")
            appendLine("                return .none")
            appendLine("                ")
            appendLine("            }")
            appendLine("        }")
            appendLine("    }")
            appendLine("}")
            appendLine()
            appendLine()

            // State Mapper
            appendLine("// MARK: - StoreState → Feature.State Mapper")
            appendLine("extension ${featureName}Feature.State {")
            appendLine("    mutating func apply(from domain: ${storeName}.State) {")
            for (prop in stateProperties) {
                val conversion = if (prop.type.declaration.simpleName.asString() in listOf("Int", "Long"))  {
                    "Int(domain.${prop.name})"
                } else {
                    "domain.${prop.name}"
                }
                appendLine("        self.${prop.name} = $conversion")
            }
            appendLine("    }")
            appendLine("}")
            appendLine()

            // Bridge Reducer
            appendLine("@Reducer")
            appendLine("struct ${storeName}BridgeReducer {")
            appendLine("    ")
            appendLine("    struct State : Equatable {}")
            appendLine("    ")
            appendLine("    @CasePathable")
            appendLine("    enum Action : Equatable {")
            appendLine("        case startObserving")
            appendLine("        case stopObserving")
            appendLine("        case stateUpdated(${storeName}.State)")
            appendLine("        case sideEffect(StoreSideEffectWrapper<${storeName}SideEffect>)")
            appendLine("    }")
            appendLine("    ")
            appendLine("    @Dependency(\\.${storeName.toCamelCase()}) var store")
            appendLine("    ")
            appendLine("    var body: some Reducer<State, Action> {")
            appendLine("        Reduce { state, action in")
            appendLine("            switch action {")
            appendLine("            case .startObserving:")
            appendLine("                return observe()")
            appendLine()
            appendLine("            case .stopObserving:")
            appendLine("              return .merge(")
            appendLine("                .cancel(id: CancelID.state),")
            appendLine("                .cancel(id: CancelID.sideEffects)")
            appendLine("              )")
            appendLine()
            appendLine("            default:")
            appendLine("              return .none")
            appendLine("            }")
            appendLine("        }")
            appendLine("    }")
            appendLine("}")
            appendLine()
            appendLine()

            // Observable Bridge
            appendLine("// MARK: - Observable Bridge")
            appendLine("extension ${storeName}BridgeReducer {")
            appendLine("    ")
            appendLine("    private enum CancelID { case state, sideEffects }")
            appendLine("    ")
            appendLine("    func observe() -> Effect<Action> {")
            appendLine("        .merge(")
            appendLine("            observeState(),")
            appendLine("            observeSideEffects()")
            appendLine("        )")
            appendLine("    }")
            appendLine("    ")
            appendLine("    private func observeState() -> Effect<Action> {")
            appendLine("        .run { send in")
            appendLine("            for try await state in asAsyncThrowingStream(CStateFlow<${storeName}.State>(source: store.states)) {")
            appendLine("                await send(.stateUpdated(state))")
            appendLine("            }")
            appendLine("        }")
            appendLine("        .cancellable(id: CancelID.state, cancelInFlight: false)")
            appendLine("    }")
            appendLine("    ")
            appendLine("    private func observeSideEffects() -> Effect<Action> {")
            appendLine("        .run { send in")
            appendLine("            for try await sideEffect in asAsyncThrowingStream(CFlow<${storeName}SideEffect>(source: store.sideEffects)) {")
            appendLine("                await send(.sideEffect(StoreSideEffectWrapper(wrapped: sideEffect)))")
            appendLine("            }")
            appendLine("        }")
            appendLine("        .cancellable(id: CancelID.sideEffects, cancelInFlight: false)")
            appendLine("    }")
            appendLine("}")
            appendLine()
            appendLine()

            // Factory Extension
            appendLine("// MARK: - Factory")
            appendLine("extension ${featureName}Feature {")
            appendLine("    ")
            appendLine("    static func from(")
            appendLine("        store: ${storeName},")
            appendLine("        withDependencies configureDependencies: @escaping (inout DependencyValues) -> Void = { _ in }")
            appendLine("    ) -> StoreOf<Self> {")
            appendLine("        let tcaStore = Store(")
            appendLine("            initialState: State(")
            for (prop in stateProperties) {
                val conversion = if (prop.type.declaration.simpleName.asString() in listOf("Int", "Long"))  {
                    "Int(store.state.${prop.name})"
                } else {
                    "store.state.${prop.name}"
                }
                appendLine("                ${prop.name}: $conversion,")
            }
            appendLine("                _bridge: ${storeName}BridgeReducer.State(),")
            appendLine("                _lifecycle: nil")  // новое
            appendLine("            )")
            appendLine("        ) {")
            appendLine("            ${featureName}Feature()")
            appendLine("        } withDependencies: { deps in")
            appendLine("            deps.${storeName.toCamelCase()} = store")
            appendLine("            configureDependencies(&deps)")
            appendLine("        }")
            appendLine()
            appendLine("        let lifecycle = _${storeName}Lifecycle(store: store) { action in")
            appendLine("            await tcaStore.send(action)")
            appendLine("        }")
            appendLine("        Task { await tcaStore.send(._setLifecycle(lifecycle)) }")
            appendLine()
            appendLine("        return tcaStore")
            appendLine("    }")
            appendLine("}")
            appendLine()
            appendLine("// MARK: - Equatable")
            appendLine("extension ${featureName}Feature.State {")
            appendLine("    static func == (lhs: Self, rhs: Self) -> Bool {")
            for (index in stateProperties.indices) {
                val prop = stateProperties[index]
                val typeName = prop.type.declaration.simpleName.asString()
                val isKotlinType = typeName in listOf(
                    "Array", "MutableList", "ArrayList",
                    "MutableSet", "HashSet", "LinkedHashSet",
                    "MutableMap", "HashMap", "LinkedHashMap"
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
            appendLine("// MARK: - Lifecycle Token")
            appendLine("final class _${storeName}Lifecycle {")
            appendLine("    private let store: ${storeName}")
            appendLine("    private var observerTask: Task<Void, Never>?")
            appendLine()
            appendLine("    init(store: ${storeName}, send: @escaping (${featureName}Feature.Action) async -> Void) {")
            appendLine("        self.store = store")
            appendLine("        store.doInit()")
            appendLine()
            appendLine("        observerTask = Task {")
            appendLine("            await withTaskGroup(of: Void.self) { group in")
            appendLine("                group.addTask {")
            appendLine("                    do {")
            appendLine("                        for try await state in asAsyncThrowingStream(CStateFlow<${storeName}.State>(source: store.states)) {")
            appendLine("                            await send(._bridge(.stateUpdated(state)))")
            appendLine("                        }")
            appendLine("                    } catch {}")
            appendLine("                }")
            appendLine()
            appendLine("                group.addTask {")
            appendLine("                    do {")
            appendLine("                        for try await effect in asAsyncThrowingStream(CFlow<${storeName}SideEffect>(source: store.sideEffects)) {")
            appendLine("                            await send(._bridge(.sideEffect(StoreSideEffectWrapper(wrapped: effect))))")
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