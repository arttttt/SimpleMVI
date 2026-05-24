# Store

The `Store` is designed to encapsulate different logic in applications. It allows to design apps using unidirectional data flow and a single source of truth.

## Key Characteristics

- `Store` has its own `State`
- It can accept `Intent`
- It can emit `SideEffect`

## Lifecycle

- Must be initialized before use by calling `Store.init()`
- Must be destroyed by calling `Store.destroy()` when no longer needed to free up resources

## Key Components

### Actor

Every `Store` must have an `Actor`. The `Actor` contains all `Store` logic and handles the `Intent` that you pass to a `Store`.

### StorePlugin

`Store` also supports `StorePlugin`. Plugins extend the store by observing its lifecycle and by participating in intent processing — a plugin can rewrite or drop an `Intent` before it reaches the `Actor`. Plugins also see state changes, side effects, and destruction. The list of attached plugins is exposed via `Store.plugins`.

## Interface

```kotlin
public interface Store<Intent : Any, State : Any, SideEffect : Any> :
    PluginsOwner<Intent, State, SideEffect> {

    /** Returns Store state */
    public val state: State

    /** Returns Store states Flow */
    public val states: StateFlow<State>

    /** Returns Store side effects Flow */
    public val sideEffects: Flow<SideEffect>

    /** Plugins attached to this Store, in invocation order */
    public val plugins: List<StorePlugin<Intent, State, SideEffect>>

    /**
     * Initializes the Store
     */
    public fun init()

    /**
     * Accepts an Intent and passes it through the plugin pipeline,
     * then to the Actor (unless a plugin blocks it).
     */
    public fun accept(intent: Intent)

    /**
     * Destroys the Store. Store cannot be used after it was destroyed
     */
    public fun destroy()
}
```

> **Note:** `Store` is invariant in `Intent`, `State`, and `SideEffect` (since 0.8.0). Earlier versions used `in`/`out` projections; that was changed because plugins need both read and write access to all three.

## Important Notes

- After calling `destroy()`, the Store cannot be used anymore.
- When a new State is emitted, every plugin is notified through `onStateChanged`.
- When a SideEffect is emitted, every plugin is notified through `onSideEffect`.
- Intents pass through the plugin pipeline before reaching the actor — see `StorePlugin` and `Pipeline`.
- Background work should be handled inside Actors using coroutines with the provided `CoroutineScope`.

For more information, refer to the documentation for `Actor` and `StorePlugin`.