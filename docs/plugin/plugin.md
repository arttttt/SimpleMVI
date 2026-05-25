# StorePlugin

`StorePlugin` extends a `Store` without touching its `Actor`. A plugin can observe the lifecycle, transform the initial state, and rewrite or drop intents before they reach the actor. Multiple plugins compose in registration order to form a pipeline.

> Coming from `Middleware`? See the [0.7 → 0.8 migration guide](../guides/migration-0.7-to-0.8.md).

## Overview

A plugin can:

- Override the initial state (`provideInitialState`)
- React to store initialization (`onInit`)
- Intercept intents — pass them through, transform them, or block them (`onIntent` + `Pipeline`)
- Track state changes (`onStateChanged`)
- Track side effects (`onSideEffect`)
- React to store destruction (`onDestroy`)

Unlike the old `Middleware`, plugins are **active participants**: they can dispatch intents, set state, emit side effects, and short-circuit the intent flow.

## Interface

```kotlin
public interface StorePlugin<Intent : Any, State : Any, SideEffect : Any> {

    public data class Context<Intent : Any, State : Any, SideEffect : Any>(
        val scope: CoroutineScope,
        val sendIntent: (Intent) -> Unit,
        val setState: (State) -> Unit,
        val sendSideEffect: (SideEffect) -> Unit,
        private val getState: () -> State,
    ) {
        val state: State
    }

    public fun provideInitialState(defaultState: State): State = defaultState

    public fun onInit(context: Context<Intent, State, SideEffect>) {}
    public fun Pipeline<Intent>.onIntent(intent: Intent) {}
    public fun onStateChanged(oldState: State, newState: State) {}
    public fun onSideEffect(sideEffect: SideEffect) {}
    public fun onDestroy() {}
}
```

Every method has a default empty implementation — implement only what you need.

## Lifecycle hooks

### provideInitialState

```kotlin
public fun provideInitialState(defaultState: State): State
```

Called once, before the store starts. Plugins are folded over the initial state in registration order — each one receives the previous plugin's output. Use it to hydrate state from persistence or apply layered defaults.

### onInit

```kotlin
public fun onInit(context: Context<Intent, State, SideEffect>)
```

Called when the store is initialized, before any intents are processed. The `Context` is the only handle a plugin has to the store at runtime — cache it if you need it in later callbacks.

`Context` exposes:

| Property | Purpose |
|---|---|
| `scope` | `CoroutineScope` tied to the store lifecycle. Cancelled in `onDestroy`. |
| `sendIntent` | Dispatch a new `Intent` to the store. |
| `setState` | Replace the current state. Triggers `onStateChanged` on every plugin. |
| `sendSideEffect` | Emit a `SideEffect` through the store. |
| `state` | Read the current state (always returns the latest value). |

### onIntent (with Pipeline)

```kotlin
public fun Pipeline<Intent>.onIntent(intent: Intent)
```

Called when the store receives an intent, before the actor sees it. The receiver is a `Pipeline<Intent>` that controls the fate of the intent:

```kotlin
public class Pipeline<Intent : Any> {
    public fun block()                       // drop the intent
    public fun transform(newIntent: Intent)  // rewrite the intent
}
```

- **Do nothing** — the intent passes to the next plugin and ultimately the actor.
- **`transform(newIntent)`** — replace the intent. Downstream plugins and the actor see `newIntent`.
- **`block()`** — drop the intent. No further plugins are called for it, and the actor is not invoked.

### onStateChanged

```kotlin
public fun onStateChanged(oldState: State, newState: State)
```

Fires after every state change — both from the actor's `reduce` blocks and from any plugin calling `Context.setState`.

### onSideEffect

```kotlin
public fun onSideEffect(sideEffect: SideEffect)
```

Fires when the store emits a side effect, before it reaches subscribers of `Store.sideEffects`.

### onDestroy

```kotlin
public fun onDestroy()
```

Called when the store is being destroyed. The store's `CoroutineScope` is still active here — use it to flush buffered work. After this returns, the scope is cancelled.

## Plugin order

The store invokes plugins in the order they were passed to `createStore`. This matters most for `onIntent`: an earlier plugin can `transform` or `block` an intent before later plugins observe it.

If you supply a non-null `name` and a logger via `configureSimpleMVI`, an automatic `LoggingPlugin` is prepended ahead of your plugins.

## Use cases

- **Logging and analytics** — observe init, intents, state changes, side effects, destroy (see `LoggingPlugin`).
- **State persistence** — hydrate state in `provideInitialState`, write it back in `onStateChanged`.
- **Intent debouncing or rate limiting** — `block` duplicate intents from `onIntent`.
- **Intent rewriting** — `transform` legacy intents into newer ones, or wrap them with metadata.
- **Feature gating** — block intents when a feature flag is off.
- **External event sourcing** — collect a flow inside `onInit` and call `sendIntent` from it.

## Examples

### Simple logging plugin

```kotlin
class SimpleLoggingPlugin<Intent : Any, State : Any, SideEffect : Any>(
    private val tag: String,
) : StorePlugin<Intent, State, SideEffect> {

    override fun onInit(context: StorePlugin.Context<Intent, State, SideEffect>) {
        println("[$tag] initialized with state: ${context.state}")
    }

    override fun Pipeline<Intent>.onIntent(intent: Intent) {
        println("[$tag] intent: $intent")
    }

    override fun onStateChanged(oldState: State, newState: State) {
        println("[$tag] $oldState -> $newState")
    }

    override fun onSideEffect(sideEffect: SideEffect) {
        println("[$tag] side effect: $sideEffect")
    }

    override fun onDestroy() {
        println("[$tag] destroyed")
    }
}
```

### Analytics plugin (passive observer)

```kotlin
class AnalyticsPlugin<Intent : Any, State : Any, SideEffect : Any>(
    private val analytics: AnalyticsService,
) : StorePlugin<Intent, State, SideEffect> {

    override fun onInit(context: StorePlugin.Context<Intent, State, SideEffect>) {
        analytics.logEvent("store_initialized")
    }

    override fun Pipeline<Intent>.onIntent(intent: Intent) {
        analytics.logEvent("intent_received", mapOf("intent" to intent::class.simpleName))
    }

    override fun onStateChanged(oldState: State, newState: State) {
        analytics.logEvent("state_changed")
    }

    override fun onSideEffect(sideEffect: SideEffect) {
        analytics.logEvent("side_effect", mapOf("effect" to sideEffect::class.simpleName))
    }

    override fun onDestroy() {
        analytics.logEvent("store_destroyed")
    }
}
```

### Intent transformer

Rewrite a deprecated intent into the new one without touching the actor:

```kotlin
class LegacyIntentMigrationPlugin : StorePlugin<MyIntent, MyState, MySideEffect> {

    override fun Pipeline<MyIntent>.onIntent(intent: MyIntent) {
        if (intent is MyIntent.LegacyLoad) {
            transform(MyIntent.Load(source = "legacy"))
        }
    }
}
```

### Intent blocker (rate limiting)

```kotlin
class RateLimitPlugin(
    private val minInterval: Duration,
) : StorePlugin<MyIntent, MyState, MySideEffect> {

    private var lastAccepted: Instant = Instant.DISTANT_PAST

    override fun Pipeline<MyIntent>.onIntent(intent: MyIntent) {
        if (intent !is MyIntent.Refresh) return
        val now = Clock.System.now()
        if (now - lastAccepted < minInterval) {
            block()
        } else {
            lastAccepted = now
        }
    }
}
```

### State hydration

```kotlin
class PersistedStatePlugin(
    private val storage: KeyValueStorage,
) : StorePlugin<MyIntent, MyState, MySideEffect> {

    override fun provideInitialState(defaultState: MyState): MyState {
        return storage.read<MyState>(KEY) ?: defaultState
    }

    override fun onStateChanged(oldState: MyState, newState: MyState) {
        storage.write(KEY, newState)
    }

    private companion object {
        const val KEY = "my_state"
    }
}
```

## Registering plugins

Plugins are registered on the store via `createStore`:

```kotlin
val store = createStore(
    name = storeName<MyStore>(),
    initialState = MyState(),
    actor = myActor,
    plugins = listOf(
        analyticsPlugin,
        rateLimitPlugin,
        persistedStatePlugin,
    ),
)
```

The order matters: in this example, `analyticsPlugin` sees the original intent, then `rateLimitPlugin` can block it, and only intents that survive reach `persistedStatePlugin`.

## Notes

- Plugin callbacks are invoked synchronously on the thread driving the corresponding store operation.
- Implement plugin hooks efficiently — slow plugin code slows down every intent, state change, and side effect.
- A plugin that needs background work should use `Context.scope`, which is cancelled in `onDestroy`.
- Multiple plugins can be registered on a single store. Each is invoked at every hook in registration order.
