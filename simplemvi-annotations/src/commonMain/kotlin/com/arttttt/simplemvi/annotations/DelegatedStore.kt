package com.arttttt.simplemvi.annotations

/**
 * Annotation for generating type-safe intent handlers for [Store] implementations
 *
 * When applied to a [Store] class, this annotation triggers code generation
 * via KSP (Kotlin Symbol Processing) that creates:
 * - An [IntentHandler] interface specific to the store's intent types
 * - A factory function for creating intent handlers in a type-safe manner
 *
 * ## Generated Code
 *
 * For a store named `MyStore`, the annotation generates:
 * - Interface: `MyStoreIntentHandler<I : MyStore.Intent>`
 * - Factory function: `myStoreIntentHandler<I : MyStore.Intent>(block: ActorScope<...>.(I) -> Unit)`
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
 * // Generated code can be used like this:
 * val incrementHandler = counterStoreIntentHandler<CounterStore.Intent.Increment> { intent ->
 *     reduce { copy(counter = counter + 1) }
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
 * The generated interface extends [IntentHandler] and provides type-safe
 * intent handling for the specific store.
 *
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class DelegatedStore