import org.jetbrains.kotlin.DeprecatedForRemovalCompilerApi
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeProjection
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.name.FqName
import java.io.File

@OptIn(ExperimentalCompilerApi::class)
class StoreGraphPlugin : CompilerPluginRegistrar() {

    companion object {
        val KEY_OUTPUT_DIR = CompilerConfigurationKey<String>("storeGraphOutputDir")
    }

    override val pluginId: String = "com.arttttt.simplemvi.compiler.storegraph"

    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val outputDir = configuration.get(KEY_OUTPUT_DIR)
            ?: "build/generated/store-graphs"

        IrGenerationExtension.registerExtension(
            StoreGraphIrExtension(outputDir)
        )
    }
}

@OptIn(ExperimentalCompilerApi::class)
class StoreGraphCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = "com.arttttt.simplemvi.compiler.storegraph"

    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        CliOption(
            optionName = "storeGraphOutputDir",
            valueDescription = "directory",
            description = "Output directory for generated .mermaid files",
            required = false,
            allowMultipleOccurrences = false,
        )
    )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration,
    ) {
        when (option.optionName) {
            "storeGraphOutputDir" -> configuration.put(StoreGraphPlugin.KEY_OUTPUT_DIR, value)
        }
    }
}

class StoreGraphIrExtension(
    private val outputDir: String,
) : IrGenerationExtension {

    override fun generate(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext
    ) {
        val collector = StoreGraphCollector()

        moduleFragment.transform(
            StoreGraphIrTransformer(pluginContext, collector),
            null
        )

        collector.graphs.forEach { (storeName, graph) ->
            MermaidGenerator.generate(storeName, graph, outputDir)
        }
    }
}

data class StoreGraph(
    val intents: MutableMap<String, IntentNode> = mutableMapOf(),
)

class StoreGraphIrTransformer(
    private val pluginContext: IrPluginContext,
    private val collector: StoreGraphCollector,
) : IrElementTransformerVoid() {

    private var currentIntent: IrClass? = null
    private var reduceDepth: Int = 0
    private val stateFieldAccess = mutableSetOf<String>()

    override fun visitFunction(declaration: IrFunction): IrStatement {
        val previousIntent = currentIntent
        if (declaration.isIntentHandler()) {
            currentIntent = declaration.intentClassOrNull()
        }
        return super.visitFunction(declaration).also {
            currentIntent = previousIntent
        }
    }

    @OptIn(DeprecatedForRemovalCompilerApi::class)
    override fun visitCall(expression: IrCall): IrExpression {
        val intentCls = currentIntent
        val storeName = intentCls?.enclosingStoreName()
        val intentName = intentCls?.fqNameWhenAvailable?.asString() ?: intentCls?.name?.asString()
        val funcName = expression.symbol.owner.name.asString()

        if (reduceDepth > 0 && funcName == "copy") {
            val params = expression.symbol.owner.valueParameters
            for (i in params.indices) {
                if (expression.getValueArgument(i) != null) {
                    stateFieldAccess += params[i].name.asString()
                }
            }
        }

        if (storeName == null || intentName == null) {
            return super.visitCall(expression)
        }

        return when (funcName) {
            "reduce" -> {
                val outerFields = stateFieldAccess.toSet()
                stateFieldAccess.clear()
                reduceDepth++
                val result = super.visitCall(expression)
                reduceDepth--
                val collected = stateFieldAccess.toList()
                stateFieldAccess.clear()
                stateFieldAccess += outerFields

                collector.recordReduce(
                    store = storeName,
                    intent = intentName,
                    changedFields = collected,
                )
                result
            }
            "postSideEffect", "sideEffect" -> {
                extractSideEffectType(expression)?.let { sideEffect ->
                    collector.recordSideEffect(
                        store = storeName,
                        intent = intentName,
                        sideEffect = sideEffect,
                    )
                }
                super.visitCall(expression)
            }
            "onNewIntent", "intent" -> {
                extractIntentType(expression)?.let { targetIntent ->
                    collector.recordIntentDispatch(
                        store = storeName,
                        fromIntent = intentName,
                        toIntent = targetIntent,
                    )
                }
                super.visitCall(expression)
            }
            else -> super.visitCall(expression)
        }
    }

    override fun visitWhen(expression: IrWhen): IrExpression {
        val intentCls = currentIntent
        val storeName = intentCls?.enclosingStoreName()
        val intentName = intentCls?.fqNameWhenAvailable?.asString() ?: intentCls?.name?.asString()
        if (storeName != null && intentName != null) {
            expression.branches.forEach { branch ->
                val condition = branch.condition
                if (referencesState(condition)) {
                    collector.recordConditional(
                        store = storeName,
                        intent = intentName,
                        condition = condition.dump()
                    )
                }
            }
        }
        return super.visitWhen(expression)
    }

}

data class IntentNode(
    val name: String,
    val reduces: MutableList<ReduceInfo> = mutableListOf(),
    val sideEffects: MutableList<String> = mutableListOf(),
    val dispatchedIntents: MutableList<String> = mutableListOf(),
    val conditionals: MutableList<String> = mutableListOf(),
)

data class ReduceInfo(
    val changedFields: List<String>,
)

class StoreGraphCollector {
    val graphs = mutableMapOf<String, StoreGraph>()

    fun recordReduce(store: String, intent: String, changedFields: List<String>) {
        val graph = graphs.getOrPut(store) { StoreGraph() }
        val node = graph.intents.getOrPut(intent) { IntentNode(intent) }
        node.reduces.add(ReduceInfo(changedFields))
    }

    fun recordSideEffect(store: String, intent: String, sideEffect: String) {
        val graph = graphs.getOrPut(store) { StoreGraph() }
        val node = graph.intents.getOrPut(intent) { IntentNode(intent) }
        node.sideEffects.add(sideEffect)
    }

    fun recordIntentDispatch(store: String, fromIntent: String, toIntent: String) {
        val graph = graphs.getOrPut(store) { StoreGraph() }
        val node = graph.intents.getOrPut(fromIntent) { IntentNode(fromIntent) }
        node.dispatchedIntents.add(toIntent)
    }

    fun recordConditional(store: String, intent: String, condition: String) {
        val graph = graphs.getOrPut(store) { StoreGraph() }
        val node = graph.intents.getOrPut(intent) { IntentNode(intent) }
        node.conditionals.add(condition)
    }
}

object MermaidGenerator {
    fun generate(storeName: String, graph: StoreGraph, outputDir: String) {
        val file = File(outputDir, "$storeName.mermaid")
        file.parentFile.mkdirs()

        file.writeText(buildString {
            appendLine("stateDiagram-v2")
            appendLine("    direction LR")
            appendLine("    [*] --> State")
            appendLine()

            graph.intents.forEach { (intentName, node) ->
                val intent = intentName.simpleName()

                node.reduces.forEach { reduce ->
                    val fields = reduce.changedFields.joinToString(", ")
                    appendLine("    $intent --> State: reduce {$fields} (${node.reduces.size}x)")
                }

                node.sideEffects.forEach { effect ->
                    val count = node.sideEffects.count { it == effect }
                    appendLine("    $intent --> ${effect.simpleName()}: postSideEffect (${count}x)")
                }

                node.dispatchedIntents.forEach { targetIntent ->
                    appendLine("    $intent --> ${targetIntent.simpleName()}: intent")
                }

                if (node.conditionals.isNotEmpty()) {
                    appendLine("    note right of $intent")
                    node.conditionals.forEach { cond ->
                        appendLine("        $cond")
                    }
                    appendLine("    end note")
                }
            }
        })
    }
}

private val STORE_FQN = FqName("com.arttttt.simplemvi.store.Store")
private val ACTOR_SCOPE_FQN = FqName("com.arttttt.simplemvi.actor.ActorScope")

private fun String.simpleName(): String = substringAfterLast('.')

/**
 * Проверяет, реализует ли класс интерфейс Store
 */
fun IrClass.implementsStore(): Boolean {
    return superTypes.any { superType ->
        val classifier = superType.classifierOrNull?.owner as? IrClass
        classifier?.fqNameWhenAvailable == STORE_FQN
    }
}

/**
 * Проверяет, является ли функция intent handler'ом
 * Intent handler — это lambda внутри actorDsl { onIntent<T> { ... } }
 * или функция с receiver типа ActorScope<Intent, State, SideEffect>
 */
@OptIn(DeprecatedForRemovalCompilerApi::class)
fun IrFunction.isIntentHandler(): Boolean {
    val receiverType = extensionReceiverParameter?.type ?: return false
    val receiverClass = receiverType.classOrNull?.owner as? IrClass ?: return false

    return receiverClass.fqNameWhenAvailable == ACTOR_SCOPE_FQN
}

/**
 * Извлекает тип Intent из функции или выражения
 */
@OptIn(DeprecatedForRemovalCompilerApi::class)
fun IrFunction.extractIntentType(): String? {
    // Intent handler имеет параметр типа Intent
    val intentParam = valueParameters.firstOrNull() ?: return null
    return intentParam.type.renderTypeName()
}

/**
 * Returns the IrClass of the intent parameter of an intent-handler function (`handle(intent: T)`),
 * or null if the parameter is unresolved (e.g. unsubstituted generic `T`).
 */
@OptIn(DeprecatedForRemovalCompilerApi::class)
fun IrFunction.intentClassOrNull(): IrClass? {
    val intentParam = valueParameters.firstOrNull() ?: return null
    return intentParam.type.classOrNull?.owner as? IrClass
}

/**
 * Walks the enclosing-class chain of this class and returns the name of the first ancestor that
 * implements [com.arttttt.simplemvi.store.Store]. Returns null if no such ancestor exists.
 *
 * This lets us infer "which store does this intent belong to" without relying on visitor traversal
 * order (which is unreliable across files and inlined call sites).
 */
fun IrClass.enclosingStoreName(): String? {
    var parent = parent as? IrClass
    while (parent != null) {
        if (parent.implementsStore()) return parent.name.asString()
        parent = parent.parent as? IrClass
    }
    return null
}

/**
 * Извлекает тип Intent из вызова intent()/onNewIntent()
 */
@OptIn(DeprecatedForRemovalCompilerApi::class)
fun extractIntentType(call: IrCall): String? {
    val arg = call.getValueArgument(0) ?: return null
    return arg.type.renderTypeName()
}

/**
 * Извлекает тип SideEffect из вызова postSideEffect()/sideEffect()
 */
@OptIn(DeprecatedForRemovalCompilerApi::class)
fun extractSideEffectType(call: IrCall): String? {
    val arg = call.getValueArgument(0) ?: return null
    return arg.type.renderTypeName()
}

/**
 * Проверяет, обращается ли выражение к state
 */
fun referencesState(expression: IrExpression): Boolean {
    var found = false

    expression.acceptVoid(object : IrVisitorVoid() {
        override fun visitGetValue(expression: IrGetValue) {
            // Ищем обращение к переменной с именем "state"
            if (expression.symbol.owner.name.asString() == "state") {
                found = true
            }
            super.visitGetValue(expression)
        }

        override fun visitCall(expression: IrCall) {
            // Ищем вызов getState()
            if (expression.symbol.owner.name.asString() == "getState") {
                found = true
            }
            super.visitCall(expression)
        }
    })

    return found
}

/**
 * Рендерит тип в читаемую строку (например, "CounterStore.Intent.Increment")
 */
fun IrType.renderTypeName(): String {
    val classifier = classifierOrNull?.owner as? IrClass ?: return "Unknown"

    val fqName = classifier.fqNameWhenAvailable?.asString() ?: classifier.name.asString()

    if (this is IrSimpleType && arguments.isNotEmpty()) {
        val typeArgs = arguments.joinToString(", ") { arg ->
            (arg as? IrTypeProjection)?.type?.renderTypeName() ?: "*"
        }
        return "$fqName<$typeArgs>"
    }

    return fqName
}

fun IrClass.extractStoreGenericTypes(): Triple<IrType, IrType, IrType>? {
    val storeType = superTypes.firstOrNull { superType ->
        val classifier = superType.classifierOrNull?.owner as? IrClass
        classifier?.fqNameWhenAvailable == STORE_FQN
    } as? IrSimpleType ?: return null

    if (storeType.arguments.size != 3) return null

    val intentType = (storeType.arguments[0] as? IrTypeProjection)?.type ?: return null
    val stateType = (storeType.arguments[1] as? IrTypeProjection)?.type ?: return null
    val sideEffectType = (storeType.arguments[2] as? IrTypeProjection)?.type ?: return null

    return Triple(intentType, stateType, sideEffectType)
}

/**
 * Извлекает nested классы Intent/State/SideEffect из Store класса
 */
fun IrClass.findNestedClass(simpleName: String): IrClass? {
    return declarations
        .filterIsInstance<IrClass>()
        .firstOrNull { it.name.asString() == simpleName }
}