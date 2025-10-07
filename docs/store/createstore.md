# Creating Stores

## createStore Function

The `createStore` function is the primary way to create a `Store` instance:

```kotlin
public fun <Intent : Any, State : Any, SideEffect : Any> createStore(
    name: StoreName?,
    initialize: Boolean = true,
    coroutineContext: CoroutineContext = Dispatchers.Main.immediate,
    initialState: State,
    initialIntents: List<Intent> = emptyList(),
    middlewares: List<Middleware<Intent, State, SideEffect>> = emptyList(),
    actor: Actor<Intent, State, SideEffect>,
): Store<Intent, State, SideEffect>
```

## Parameters

### name (required)
**Type:** `StoreName?`

The name of the store, used for logging and debugging. Use the `storeName<T>()` helper function:

```kotlin
name = storeName<MyStore>()
```

You can also pass `null` to disable automatic logging:

```kotlin
name = null  // No automatic logging middleware
```

**Note:** If you provide a non-null `name` and have configured a logger via `configureSimpleMVI`, a `LoggingMiddleware` will be automatically added to your store.

### initialState (required)
**Type:** `State`

The initial state of the store. This is the state before any intents are processed:

```kotlin
initialState = MyState(
    counter = 0,
    isLoading = false
)
```

**Best practices:**

- Use immutable data classes for state
- Provide sensible defaults for all properties
- Keep state structure flat when possible

### actor (required)
**Type:** `Actor<Intent, State, SideEffect>`

The actor that contains all business logic. Can be created using three approaches:

```kotlin
// 1. Using actorDsl
actor = actorDsl {
    onIntent<MyIntent.DoSomething> { /* ... */ }
}

// 2. Using DefaultActor
actor = MyCustomActor()

// 3. Using delegatedActor with explicit handlers
actor = delegatedActor(
    intentHandlers = listOf(
        myIntentHandler1(),
        myIntentHandler2()
    )
)
```

For more details on Actor implementations, see the Actor documentation.

### initialize (optional)
**Type:** `Boolean`  
**Default:** `true`

Whether to automatically call `Store.init()` after creation:

```kotlin
// Automatic initialization (default)
val store = createStore(
    name = storeName<MyStore>(),
    initialize = true,  // Store is ready to use immediately
    initialState = MyState(),
    actor = myActor
)
store.accept(MyIntent.DoSomething)  // Can use immediately

// Manual initialization
val store = createStore(
    name = storeName<MyStore>(),
    initialize = false,  // Must call init() manually
    initialState = MyState(),
    actor = myActor
)
store.init()  // Must call this before using
store.accept(MyIntent.DoSomething)
```

**When to use manual initialization:**

- When you need to delay store initialization until a specific time
- In testing scenarios where you want precise control over initialization
- When integrating with lifecycle-aware components

### coroutineContext (optional)
**Type:** `CoroutineContext`  
**Default:** `Dispatchers.Main.immediate`

The coroutine context used by the store for launching coroutines within the actor:

```kotlin
// Default: Main dispatcher (recommended for UI-related stores)
createStore(
    name = storeName<MyStore>(),
    coroutineContext = Dispatchers.Main.immediate,
    initialState = MyState(),
    actor = myActor
)

// Custom context for specific use cases
createStore(
    name = storeName<MyStore>(),
    coroutineContext = Dispatchers.Default + SupervisorJob(),
    initialState = MyState(),
    actor = myActor
)
```

**Note:** The default `Dispatchers.Main.immediate` is recommended for stores that interact with UI, as it ensures state updates are immediately available to the UI layer. For background-only stores, you may choose a different dispatcher.

### initialIntents (optional)
**Type:** `List<Intent>`  
**Default:** `emptyList()`

A list of intents to process immediately after initialization:

```kotlin
createStore(
    name = storeName<MyStore>(),
    initialState = MyState(),
    actor = myActor,
    initialIntents = listOf(
        MyIntent.LoadInitialData,
        MyIntent.CheckAuthStatus
    )
)
```

This is equivalent to:
```kotlin
val store = createStore(...)
store.accept(MyIntent.LoadInitialData)
store.accept(MyIntent.CheckAuthStatus)
```

**Use cases:**

- Loading initial data when the store starts
- Triggering startup tasks
- Setting up subscriptions or listeners

### middlewares (optional)
**Type:** `List<Middleware<Intent, State, SideEffect>>`  
**Default:** `emptyList()`

A list of middleware to observe store events:

```kotlin
createStore(
    name = storeName<MyStore>(),
    initialState = MyState(),
    actor = myActor,
    middlewares = listOf(
        analyticsMiddleware,
        customLoggingMiddleware,
        performanceMiddleware
    )
)
```

**Note:** If you provide a `name` and have configured a logger via `configureSimpleMVI`, a `LoggingMiddleware` will be automatically prepended to your middleware list.

**Middleware execution order:**

1. Automatic `LoggingMiddleware` (if name is provided and logger is configured)
2. Your custom middlewares in the order they appear in the list

## Complete Example with Domain-Focused Design

```kotlin
@DelegatedStore  // Optional: enables code generation
class CounterStore : Store<CounterStore.Intent, CounterStore.State, CounterStore.SideEffect> by createStore(
    name = storeName<CounterStore>(),
    initialize = true,
    initialState = State(counter = 0),
    initialIntents = listOf(Intent.Initialize),
    middlewares = listOf(analyticsMiddleware),
    actor = delegatedActor(
        initHandler = InitHandler {
            // Optional: initialization logic
        },
        intentHandlers = listOf(
            incrementIntentHandler(),
            decrementIntentHandler(),
            resetIntentHandler()
        ),
        destroyHandler = DestroyHandler {
            // Optional: cleanup logic
        }
    )
) {
    sealed interface Intent {
        data object Initialize : Intent
        data object Increment : Intent
        data object Decrement : Intent
        data object Reset : Intent
    }
    
    data class State(
        val counter: Int,
        val initialized: Boolean = false
    )
    
    // Domain events, not UI actions
    sealed interface SideEffect {
        data class CounterChanged(val value: Int) : SideEffect
        data object CounterReset : SideEffect
        data object MinimumReached : SideEffect
        data object MaximumReached : SideEffect
    }
}

// Intent handlers (can be in separate files)
fun incrementIntentHandler() = counterStoreIntentHandler<CounterStore.Intent.Increment> { intent ->
    if (state.counter >= 100) {
        sideEffect(CounterStore.SideEffect.MaximumReached)
        return@counterStoreIntentHandler
    }
    
    reduce { copy(counter = counter + 1) }
    sideEffect(CounterStore.SideEffect.CounterChanged(state.counter))
}

fun decrementIntentHandler() = counterStoreIntentHandler<CounterStore.Intent.Decrement> { intent ->
    if (state.counter <= 0) {
        sideEffect(CounterStore.SideEffect.MinimumReached)
        return@counterStoreIntentHandler
    }
    
    reduce { copy(counter = counter - 1) }
    sideEffect(CounterStore.SideEffect.CounterChanged(state.counter))
}

fun resetIntentHandler() = counterStoreIntentHandler<CounterStore.Intent.Reset> { intent ->
    reduce { copy(counter = 0) }
    sideEffect(CounterStore.SideEffect.CounterReset)
}
```

## Code Generation

SimpleMVI provides KSP-based code generation to simplify working with `delegatedActor` and intent handlers.

### Setup

Add the code generation dependencies to your build file:

```kotlin
// build.gradle.kts
plugins {
    id("com.google.devtools.ksp") version "<version>"
}

dependencies {
    implementation("io.github.arttttt.simplemvi:simplemvi:<version>")
    implementation("io.github.arttttt.simplemvi:simplemvi-annotations:<version>")
    ksp("io.github.arttttt.simplemvi:simplemvi-codegen:<version>")
}
```

### @DelegatedStore Annotation

Annotate your Store with `@DelegatedStore` to generate type-safe intent handler factories:

```kotlin
@DelegatedStore
class MyStore : Store</* ... */> by createStore(/* ... */) {
    // Your Store definition
}
```

This generates:
- A type-safe intent handler factory function: `myStoreIntentHandler<T>()`
- Proper type inference for your Intent, State, and SideEffect types

**Benefits:**

- Less boilerplate code
- Type-safe intent handler creation
- Better IDE support and autocomplete
- Compile-time safety

For more details, see the Actor documentation.

## Store Operators

SimpleMVI provides convenient operators for working with stores:

### Accept Operator (+)

```kotlin
// Instead of store.accept(intent)
store + MyStore.Intent.DoSomething

// Example usage
counterStore + CounterStore.Intent.Increment
counterStore + CounterStore.Intent.Decrement
```

### Compound Assignment Operator (+=)

```kotlin
// Equivalent to store.accept(intent)
store += MyStore.Intent.DoSomething
```

These operators make code more concise and readable, especially when dispatching multiple intents.

## Store Delegation Pattern

The recommended pattern is to use store delegation with the `by` keyword:

```kotlin
class MyStore : Store<Intent, State, SideEffect> by createStore(
    name = storeName<MyStore>(),
    initialState = State(),
    actor = myActor
) {
    // Your Intent, State, SideEffect definitions here
}
```

**Benefits:**

- Clean separation of concerns
- All store functionality is automatically implemented
- Easy to add custom methods or properties if needed
- Standard pattern across the codebase

## Best Practices

1. **Always provide a name for logging during development:**
   ```kotlin
   name = storeName<MyStore>()  // Good for debugging
   ```

2. **Use descriptive initial states:**
   ```kotlin
   initialState = State(
       isLoading = false,
       data = null,
       error = null
   )
   ```

3. **Keep the actor focused on business logic:**

    - Don't mix UI logic in actors
    - Use side effects for domain events
    - Keep state updates pure

4. **Use initialIntents sparingly:**

    - Only for critical startup tasks
    - Consider if initialization logic belongs in `onInit` instead

5. **Always destroy stores when done:**
   ```kotlin
   // In a lifecycle-aware component
   override fun onDestroy() {
       super.onDestroy()
       store.destroy()
   }
   ```
   