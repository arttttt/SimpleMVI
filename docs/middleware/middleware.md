# Middleware

The `Middleware` is designed to provide a way to "spy" on the `Store` without modifying its behavior. It's useful for implementing cross-cutting concerns such as logging, analytics, or debugging.

## Overview

`Middleware` allows you to:

- Observe `Store` initialization
- Observe `Intent` received by the `Store`
- Monitor `State` changes
- Track `SideEffect` production
- Observe `Store` destruction

It's important to note that `Middleware` cannot modify the input events it observes.

## Interface Definition

```kotlin
public interface Middleware<Intent : Any, State : Any, SideEffect : Any> {
    /**
     * Called when the Store is initialized
     */
    public fun onInit(state: State)

    /**
     * Called when the Store receives a new Intent
     */
    public fun onIntent(intent: Intent, state: State)

    /**
     * Called when a new State is produced inside the Actor
     */
    public fun onStateChanged(oldState: State, newState: State)

    /**
     * Called when the Actor produces a new SideEffect
     */
    public fun onSideEffect(sideEffect: SideEffect, state: State)

    /**
     * Called when the Store is destroyed
     */
    public fun onDestroy(state: State)
}
```

## Key Methods

### onInit

```kotlin
public fun onInit(state: State)
```

This method is called when the `Store` is initialized, providing access to the initial state. This is useful for:

- Setting up analytics tracking
- Initializing performance monitoring
- Logging store creation

**Timing:** Called during `Store.init()`, before any intents are processed.

### onIntent

```kotlin
public fun onIntent(intent: Intent, state: State)
```

This method is invoked when the `Store` receives a new `Intent`. It provides access to:

- The received `Intent`
- The current `State` at the time the `Intent` was received

**Timing:** Called before the `Actor` processes the intent, allowing the middleware to observe the intent before any state changes occur.

### onStateChanged

```kotlin
public fun onStateChanged(oldState: State, newState: State)
```

This method is called when a new `State` is produced inside the `Actor`. It provides access to:

- The previous `State`
- The new `State`

This allows for easy comparison and tracking of state changes.

**Timing:** Called immediately after a state reduction, before the new state is emitted to collectors.

### onSideEffect

```kotlin
public fun onSideEffect(sideEffect: SideEffect, state: State)
```

This method is invoked when the `Actor` produces a new `SideEffect`. It provides access to:

- The produced `SideEffect`
- The current `State` at the time the `SideEffect` was produced

**Timing:** Called when a side effect is emitted but before it's delivered to collectors.

### onDestroy

```kotlin
public fun onDestroy(state: State)
```

This method is called when the `Store` is being destroyed, right before resources are released. It provides access to:

- The final `State` of the `Store`

**Important:** This is the last method called on the middleware. The `CoroutineScope` is still active when this method is called.

**Use cases:**
- Flushing analytics events
- Cleaning up middleware resources
- Logging store lifecycle completion

## Use Cases

`Middleware` can be used for various purposes, including but not limited to:

1. **Logging**: Track all events passing through the `Store` for debugging or auditing purposes.
2. **Analytics**: Capture user actions and state changes for analysis.
3. **Debugging**: Monitor the flow of `Intent`, `State`, and `SideEffect` for troubleshooting.
4. **Performance Monitoring**: Track the frequency and timing of state changes and side effects.
5. **Testing**: Verify the sequence and correctness of store events during tests.

## Example Implementation

### Simple Logging Middleware

```kotlin
class SimpleLoggingMiddleware<Intent : Any, State : Any, SideEffect : Any>(
    private val tag: String
) : Middleware<Intent, State, SideEffect> {

    override fun onInit(state: State) {
        println("[$tag] Initialized with state: $state")
    }

    override fun onIntent(intent: Intent, state: State) {
        println("[$tag] Intent: $intent")
    }

    override fun onStateChanged(oldState: State, newState: State) {
        println("[$tag] State changed from $oldState to $newState")
    }

    override fun onSideEffect(sideEffect: SideEffect, state: State) {
        println("[$tag] Side effect: $sideEffect")
    }

    override fun onDestroy(state: State) {
        println("[$tag] Destroyed with final state: $state")
    }
}
```

### Analytics Middleware

```kotlin
class AnalyticsMiddleware<Intent : Any, State : Any, SideEffect : Any>(
    private val analytics: AnalyticsService
) : Middleware<Intent, State, SideEffect> {

    override fun onInit(state: State) {
        analytics.logEvent("store_initialized")
    }

    override fun onIntent(intent: Intent, state: State) {
        analytics.logEvent(
            "intent_received",
            mapOf("intent" to intent::class.simpleName)
        )
    }

    override fun onStateChanged(oldState: State, newState: State) {
        analytics.logEvent("state_changed")
    }

    override fun onSideEffect(sideEffect: SideEffect, state: State) {
        analytics.logEvent(
            "side_effect",
            mapOf("effect" to sideEffect::class.simpleName)
        )
    }

    override fun onDestroy(state: State) {
        analytics.logEvent("store_destroyed")
    }
}
```

## Using Middleware

Middleware can be registered when creating a store:

```kotlin
val store = createStore(
    name = storeName<MyStore>(),
    initialState = MyState(),
    actor = myActor,
    middlewares = listOf(
        loggingMiddleware,
        analyticsMiddleware,
        customDebugMiddleware
    )
)
```

Multiple middleware instances can be used simultaneously, and they will be notified in the order they were registered.

## Important Notes

- `Middleware` is a passive observer and cannot modify the events it receives.
- Multiple `Middleware` instances can be used simultaneously in a single `Store`.
- `Middleware` methods should be implemented efficiently to avoid impacting the performance of the main application logic.
- While `Middleware` has access to the `State`, it should not attempt to modify it directly.
- All `Middleware` methods are called synchronously on the same thread where the store operation occurred.

For more information on how `Middleware` interacts with other components, refer to the `Store` and `Actor` documentation.