package com.arttttt.simplemvi.annotations

/**
 * Annotation for generating TCA (The Composable Architecture) Feature wrapper for SimpleMVI Store
 *
 * When applied to a [Store] class, this annotation triggers Swift code generation
 * via KSP (Kotlin Symbol Processing) that creates:
 * - A TCA Feature struct with Action/State/Effect mapping
 * - Observable bridge between SimpleMVI Store and TCA ViewStore
 * - Lifecycle-safe subscriptions to state and side effects
 *
 * ## Generated Swift Code
 *
 * For a store named `CounterStore`, the annotation generates:
 * ```swift
 * // File: CounterFeature.swift
 *
 * @Reducer
 * struct CounterFeature {
 *     struct State: Equatable {
 *         var counter: Int32
 *     }
 *
 *     enum Action: Equatable {
 *         case increment
 *         case decrement
 *         case _syncState(CounterStoreState)
 *         case _receiveSideEffect(CounterStoreSideEffect)
 *     }
 *
 *     // Bridge to SimpleMVI Store
 *     // State/SideEffect observation
 *     // Action → Intent mapping
 * }
 * ```
 *
 * ## Usage Example
 *
 * ```kotlin
 * @TCAFeature
 * class CounterStore : Store<CounterStore.Intent, CounterStore.State, CounterStore.SideEffect> {
 *     sealed interface Intent {
 *         data object Increment : Intent
 *         data object Decrement : Intent
 *     }
 *
 *     data class State(val counter: Int)
 *
 *     sealed interface SideEffect {
 *         data class CounterChanged(val value: Int) : SideEffect
 *     }
 * }
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class TCAFeature