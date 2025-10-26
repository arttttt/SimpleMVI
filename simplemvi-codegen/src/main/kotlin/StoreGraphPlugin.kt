import org.jetbrains.kotlin.DeprecatedForRemovalCompilerApi
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
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
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
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

    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val outputDir = configuration.get(KEY_OUTPUT_DIR)
            ?: "build/generated/store-graphs"

        error(outputDir)

        IrGenerationExtension.registerExtension(
            StoreGraphIrExtension(outputDir)
        )
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

    private var currentStore: String? = null
    private var currentIntent: String? = null
    private val stateFieldAccess = mutableSetOf<String>()

    override fun visitClass(declaration: IrClass): IrStatement {
        if (declaration.implementsStore()) {
            currentStore = declaration.name.asString()
        }
        return super.visitClass(declaration)
    }

    override fun visitFunction(declaration: IrFunction): IrStatement {
        if (declaration.isIntentHandler()) {
            currentIntent = declaration.extractIntentType()
        }
        return super.visitFunction(declaration)
    }

    @OptIn(DeprecatedForRemovalCompilerApi::class)
    override fun visitCall(expression: IrCall): IrExpression {
        val functionName = expression.symbol.owner.name.asString()

        when (functionName) {
            "reduce" -> {
                val lambda = expression.getValueArgument(0) as? IrFunctionExpression
                lambda?.let { analyzeReduceLambda(it) }

                collector.recordReduce(
                    store = currentStore!!,
                    intent = currentIntent!!,
                    changedFields = stateFieldAccess.toList()
                )
                stateFieldAccess.clear()
            }
            "postSideEffect", "sideEffect" -> {
                val sideEffect = extractSideEffectType(expression)
                collector.recordSideEffect(
                    store = currentStore!!,
                    intent = currentIntent!!,
                    sideEffect = sideEffect!!
                )
            }
            "onNewIntent", "intent" -> {
                val targetIntent = extractIntentType(expression)
                collector.recordIntentDispatch(
                    store = currentStore!!,
                    fromIntent = currentIntent!!,
                    toIntent = targetIntent!!
                )
            }
        }

        return super.visitCall(expression)
    }

    override fun visitWhen(expression: IrWhen): IrExpression {
        // Анализируем if/when по state
        expression.branches.forEach { branch ->
            val condition = branch.condition
            if (referencesState(condition)) {
                collector.recordConditional(
                    store = currentStore!!,
                    intent = currentIntent!!,
                    condition = condition.dump()
                )
            }
        }
        return super.visitWhen(expression)
    }

    @OptIn(DeprecatedForRemovalCompilerApi::class)
    private fun analyzeReduceLambda(lambda: IrFunctionExpression) {
        // Посетитель для copy() вызовов внутри reduce
        lambda.accept(object : IrVisitorVoid() {
            override fun visitCall(expression: IrCall) {
                if (expression.symbol.owner.name.asString() == "copy") {
                    // Извлекаем изменённые поля из copy(field1 = ..., field2 = ...)
                    for (i in 0 until expression.valueArgumentsCount) {
                        expression.getValueArgument(i)?.let {
                            val paramName = expression.symbol.owner.valueParameters[i].name.asString()
                            stateFieldAccess.add(paramName)
                        }
                    }
                }
                super.visitCall(expression)
            }
        }, null)
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
                // Reduce переходы
                node.reduces.forEach { reduce ->
                    val fields = reduce.changedFields.joinToString(", ")
                    appendLine("    $intentName --> State: reduce {$fields} (${node.reduces.size}x)")
                }

                // Side effects
                node.sideEffects.forEach { effect ->
                    val count = node.sideEffects.count { it == effect }
                    appendLine("    $intentName --> $effect: postSideEffect (${count}x)")
                }

                // Intent dispatch
                node.dispatchedIntents.forEach { targetIntent ->
                    appendLine("    $intentName --> $targetIntent: intent")
                }

                // Conditionals
                if (node.conditionals.isNotEmpty()) {
                    appendLine("    note right of $intentName")
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