package com.arttttt.simplemvi.annotations

/**
 * Annotation for generating type-safe handlers for [Store] implementations
 *
 * When applied to a [Store] class, this annotation triggers code generation
 * via KSP (Kotlin Symbol Processing) that creates:
 * - An [IntentHandler] interface specific to the store's intent types
 * - An [InitHandler] interface for store initialization
 * - A [DestroyHandler] interface for store cleanup
 * - A factory function for creating intent handlers in a type-safe manner
 * - Factory functions for creating init and destroy handlers
 *
 * ## Generated Code
 *
 * For a store named `MyStore`, the annotation generates:
 *
 * **Intent Handler:**
 * - Interface: `MyStoreIntentHandler<I : MyStore.Intent>`
 * - Factory function: `myStoreIntentHandler<I : MyStore.Intent>(block: ActorScope<...>.(I) -> Unit)`
 *
 * **Init Handler:**
 * - Interface: `MyStoreInitHandler`
 * - Factory function: `myStoreInitHandler(block: ActorScope<...>.() -> Unit)`
 *
 * **Destroy Handler:**
 * - Interface: `MyStoreDestroyHandler`
 * - Factory function: `myStoreDestroyHandler(block: ActorScope<...>.() -> Unit)`
 *
 * ## Usage Example
 *
 * ```kotlin
 * @DelegatedStore
 * class CounterStore : Store<CounterStore.Intent, CounterStore.State, CounterStore.SideEffect> {
 *     sealed interface Intent {
 *         data object Increment : Intent
 *         data object Decrement : Intent
 *     }
 *     // ... other definitions
 * }
 *
 * // Generated intent handler:
 * val incrementHandler = counterStoreIntentHandler<CounterStore.Intent.Increment> { intent ->
 *     reduce { copy(counter = counter + 1) }
 * }
 *
 * // Generated init handler:
 * val initHandler = counterStoreInitHandler {
 *     reduce { copy(initialized = true) }
 * }
 *
 * // Generated destroy handler:
 * val destroyHandler = counterStoreDestroyHandler {
 *     // Cleanup logic
 * }
 * ```
 *
 * ## Requirements
 *
 * The annotated class must:
 * - Implement the `Store` interface
 * - Have exactly three generic type parameters: Intent, State, and SideEffect
 * - Define Intent, State, and SideEffect types
 *
 * ## Code Generation Details
 *
 * The generated interfaces extend [IntentHandler], [InitHandler], and [DestroyHandler]
 * and provide type-safe handling for the specific store.
 *
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class DelegatedStore