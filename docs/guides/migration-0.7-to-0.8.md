# Migrating from 0.7 to 0.8

SimpleMVI 0.8 removes the `Middleware` API and replaces it with `StorePlugin`. This is the main breaking change in the release — most existing code can be ported mechanically. The store's coroutine context handling was also adjusted, and a few small API shapes changed as a side effect.

This guide covers everything you need to upgrade.

## `Middleware` → `StorePlugin`

The `Middleware` interface and the `middlewares` parameter on `createStore` were removed. They are replaced by `StorePlugin` and the `plugins` parameter.

### Why the change

`Middleware` was a passive observer: it could only watch store events. `StorePlugin` is an active participant — it can dispatch intents, push state, emit side effects, and rewrite or drop intents through a per-intent `Pipeline` before they reach the actor. The new design covers the old use cases and adds intent transformation, state hydration, and other extension points that middleware could not express.

See the [StorePlugin reference](../plugin/plugin.md) for the full API.

### Renames

| 0.7.x | 0.8.0 |
|---|---|
| `Middleware<I, S, SE>` | `StorePlugin<I, S, SE>` |
| `middlewares = listOf(...)` parameter on `createStore` | `plugins = listOf(...)` |
| `LoggingMiddleware` | `LoggingPlugin` (still added automatically when `name != null` and a logger is configured) |
| `com.arttttt.simplemvi.middleware` package | `com.arttttt.simplemvi.plugin` package |

### Method signatures

| 0.7.x | 0.8.0 |
|---|---|
| `fun onInit(state: State)` | `fun onInit(context: Context<...>)` — read `context.state`; cache the `context` if you need it in later callbacks |
| `fun onIntent(intent: Intent, state: State)` | `fun Pipeline<Intent>.onIntent(intent: Intent)` — read state from cached `Context`; call `transform`/`block` to alter the intent flow |
| `fun onStateChanged(oldState, newState)` | unchanged |
| `fun onSideEffect(sideEffect, state)` | `fun onSideEffect(sideEffect)` — read state from cached `Context` |
| `fun onDestroy(state: State)` | `fun onDestroy()` — read state from cached `Context` |

### Mechanical port for an observer-only middleware

If your old middleware only watched events, the port is one-to-one: cache the `Context` in `onInit` and read `context.state` wherever you used to read the `state` parameter.

**Before (0.7.x):**

```kotlin
class AnalyticsMiddleware<Intent : Any, State : Any, SideEffect : Any>(
    private val analytics: AnalyticsService,
) : Middleware<Intent, State, SideEffect> {

    override fun onInit(state: State) {
        analytics.logEvent("store_initialized", mapOf("state" to state.toString()))
    }

    override fun onIntent(intent: Intent, state: State) {
        analytics.logEvent("intent_received", mapOf("intent" to intent::class.simpleName))
    }

    override fun onStateChanged(oldState: State, newState: State) {
        analytics.logEvent("state_changed")
    }

    override fun onSideEffect(sideEffect: SideEffect, state: State) {
        analytics.logEvent("side_effect", mapOf("effect" to sideEffect::class.simpleName))
    }

    override fun onDestroy(state: State) {
        analytics.logEvent("store_destroyed", mapOf("final_state" to state.toString()))
    }
}
```

**After (0.8.0):**

```kotlin
class AnalyticsPlugin<Intent : Any, State : Any, SideEffect : Any>(
    private val analytics: AnalyticsService,
) : StorePlugin<Intent, State, SideEffect> {

    private var context: StorePlugin.Context<Intent, State, SideEffect> by Delegates.notNull()

    override fun onInit(context: StorePlugin.Context<Intent, State, SideEffect>) {
        this.context = context
        analytics.logEvent("store_initialized", mapOf("state" to context.state.toString()))
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
        analytics.logEvent("store_destroyed", mapOf("final_state" to context.state.toString()))
    }
}
```

### Updating `createStore` call sites

Rename the parameter:

```kotlin
// 0.7.x
createStore(
    name = storeName<MyStore>(),
    initialState = MyState(),
    actor = myActor,
    middlewares = listOf(analyticsMiddleware),
)

// 0.8.0
createStore(
    name = storeName<MyStore>(),
    initialState = MyState(),
    actor = myActor,
    plugins = listOf(analyticsPlugin),
)
```

If you used `LoggingMiddleware` directly, switch to `LoggingPlugin` — same constructor parameters. Most users don't need to do this manually: `createStore` still adds a `LoggingPlugin` automatically when you supply a non-null `name` and a configured logger.

## `Store` type parameters are now invariant

Before 0.8.0:

```kotlin
public interface Store<in Intent : Any, out State : Any, out SideEffect : Any>
```

In 0.8.0:

```kotlin
public interface Store<Intent : Any, State : Any, SideEffect : Any> :
    PluginsOwner<Intent, State, SideEffect>
```

The variance was relaxed because plugins need to both read and write all three type parameters (read intents via `onIntent`, write them via `Context.sendIntent`; similar for state and side effects).

**Impact:** rare. You'll only feel it if your code depended on the variance — for example, holding a `Store<*, BaseState, *>` reference that received a `Store<MyIntent, ChildState, MySideEffect>`. Such assignments now need an explicit cast or matching type parameters. Most call sites won't notice.

## `Store.plugins` is now part of the public interface

`Store` now extends `PluginsOwner`, which adds:

```kotlin
public val plugins: List<StorePlugin<Intent, State, SideEffect>>
```

You don't need to do anything to get this — every store implementation provides it. Use it for testing or diagnostics if you want to inspect the plugin chain attached to a store.

## `coroutineContext` no longer gets an extra `Job` wrapped around it

In 0.7.x, the store always wrapped the supplied `coroutineContext` in a fresh `Job`. That made it impossible to control the root `Job` — for example, to use a `SupervisorJob`.

In 0.8.0:

- `DefaultStore` uses the supplied context as-is. Whatever `Job` is in the context becomes the parent for the store's coroutines.
- `createStore` keeps the old default behaviour by including `+ Job()` in the default value of `coroutineContext`. The new default is `Dispatchers.Main.immediate + Job()`.

**Impact for most callers:** none — if you didn't pass `coroutineContext`, behaviour is identical.

**If you did pass `coroutineContext`:** you now control the `Job`. Before, your context's `Job` was hidden behind a fresh one and had no effect on child coroutines. After, you must include a `Job` (or `SupervisorJob`) yourself, otherwise the store's scope has no parent `Job` and won't cancel correctly.

```kotlin
// 0.7.x — Job was ignored, store wrapped it anyway
createStore(
    coroutineContext = Dispatchers.Default + SupervisorJob(),
    /* ... */
)

// 0.8.0 — same code, now the SupervisorJob actually applies
createStore(
    coroutineContext = Dispatchers.Default + SupervisorJob(),
    /* ... */
)
```

If you were passing a dispatcher only, add a `Job` explicitly to match the old behaviour:

```kotlin
// 0.7.x
createStore(coroutineContext = Dispatchers.IO, /* ... */)

// 0.8.0 — equivalent
createStore(coroutineContext = Dispatchers.IO + Job(), /* ... */)
```

## Checklist

- [ ] Replace every `Middleware` implementation with `StorePlugin`.
- [ ] Update method signatures: cache `Context` in `onInit`; change `onIntent` to receive `Pipeline<Intent>`; drop the `state` parameter from `onSideEffect` and `onDestroy`.
- [ ] Rename `middlewares = ...` to `plugins = ...` in every `createStore` call.
- [ ] Replace any explicit `LoggingMiddleware` references with `LoggingPlugin`.
- [ ] Update imports from `com.arttttt.simplemvi.middleware` to `com.arttttt.simplemvi.plugin`.
- [ ] If you pass a custom `coroutineContext` without a `Job`, add `+ Job()` (or use `SupervisorJob`) to preserve cancellation semantics.
- [ ] If you stored a `Store<...>` reference relying on variance, adjust the type or add a cast.
