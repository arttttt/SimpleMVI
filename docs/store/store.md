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

### Middleware

`Store` also supports `Middleware`. `Middleware` can be used as a spy - it receives all store events but cannot modify them.

## Interface

```kotlin
public interface Store<in Intent : Any, out State : Any, out SideEffect : Any> {

    /** Returns Store state */
    public val state: State

    /** Returns Store states Flow */
    public val states: StateFlow<State>

    /** Returns Store side effects Flow */
    public val sideEffects: Flow<SideEffect>

    /**
     * Initializes the Store
     * Must be called on the main thread only
     */
    @MainThread
    public fun init()

    /**
     * Accepts an Intent and passes it to the Actor
     * Intent is also available inside the Middleware
     * Must be called on the main thread only
     */
    @MainThread
    public fun accept(intent: Intent)

    /**
     * Destroys the Store. Store cannot be used after it was destroyed
     * Must be called on the main thread only
     */
    @MainThread
    public fun destroy()
}
```

## Important Notes

- All methods marked with `@MainThread` must be called on the main thread only.
- After calling `destroy()`, the Store cannot be used anymore.
- When a new State is emitted, it's available inside the Middleware.
- When a SideEffect is emitted, it's available inside the Middleware.

For more information, refer to the documentation for `Actor` and `Middleware`.