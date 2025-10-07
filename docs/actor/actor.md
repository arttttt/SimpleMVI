# Actor

The `Actor` is a crucial component of the `Store` architecture, responsible for implementing the core logic of the application.

## Overview

`Actor` is designed to:

- Accept `Intent`
- Produce `SideEffect`
- Generate new `State`

It must be part of the `Store` and is managed by it.

## Interface Definition

```kotlin
public interface Actor<Intent : Any, State : Any, out SideEffect : Any> {

    /**
     * Initializes the Actor
     * Called by the Store
     */
    public fun init(
        scope: CoroutineScope,
        getState: () -> State,
        reduce: (State.() -> State) -> Unit,
        onNewIntent: (Intent) -> Unit,
        postSideEffect: (sideEffect: SideEffect) -> Unit,
    )

    /**
     * Called when the Store receives a new Intent
     * Called by the Store
     */
    public fun onIntent(intent: Intent)

    /**
     * Destroys the Actor
     * Called by the Store
     */
    public fun destroy()
}
```

## Key Methods

### init

```kotlin
public fun init(
    scope: CoroutineScope,
    getState: () -> State,
    reduce: (State.() -> State) -> Unit,
    onNewIntent: (Intent) -> Unit,
    postSideEffect: (sideEffect: SideEffect) -> Unit,
)
```

This function initializes the `Actor`. It is called by the `Store` and provides the `Actor` with necessary dependencies:

- `scope`: A `CoroutineScope` for managing coroutines
- `getState`: A function to retrieve the current state
- `reduce`: A function to update the state
- `onNewIntent`: A function to handle new intents
- `postSideEffect`: A function to emit side effects

### onIntent

```kotlin
public fun onIntent(intent: Intent)
```

This function is called when the `Store` receives a new `Intent`. It's where the `Actor` processes incoming intents and updates the state or produces side effects accordingly.

### destroy

```kotlin
public fun destroy()
```

This function is called to destroy the `Actor`. It should be used to clean up any resources or cancel any ongoing operations.

## Actor Implementations

SimpleMVI provides three approaches for implementing actors:

### 1. DefaultActor - Object-Oriented Approach

`DefaultActor` is an abstract base class that provides a traditional OOP approach to implementing actors:

```kotlin
class CounterActor : DefaultActor<CounterIntent, CounterState, CounterSideEffect>() {

    override fun handleIntent(intent: CounterIntent) {
        when (intent) {
            is CounterIntent.Increment -> {
                reduce { copy(count = count + 1) }
            }
            is CounterIntent.Decrement -> {
                reduce { copy(count = count - 1) }
            }
            is CounterIntent.Reset -> {
                reduce { CounterState() }
                sideEffect(CounterSideEffect.CounterReset)
            }
        }
    }

    override fun onInit() {
        // Optional: initialization logic
    }

    override fun onDestroy() {
        // Optional: cleanup logic
    }
}
```

**Available protected members:**

- `state: State` - Access to current state
- `scope: CoroutineScope` - Coroutine scope for async operations
- `reduce(block: State.() -> State)` - Update state
- `sideEffect(sideEffect: SideEffect)` - Emit side effect
- `intent(intent: Intent)` - Dispatch new intent

**When to use:**

- Complex business logic requiring shared state or helper methods
- Large projects where OOP structure is beneficial
- When you prefer traditional class-based approach
- When you need to share logic between multiple intent handlers

### 2. actorDsl - Functional DSL Approach

The `actorDsl` function provides a declarative DSL for creating actors without defining a class:

```kotlin
val counterActor = actorDsl<CounterIntent, CounterState, CounterSideEffect> {
    onInit {
        // Optional: initialization logic
    }
    
    onIntent<CounterIntent.Increment> { intent ->
        reduce { copy(count = count + 1) }
    }
    
    onIntent<CounterIntent.Decrement> { intent ->
        reduce { copy(count = count - 1) }
    }
    
    onIntent<CounterIntent.Reset> { intent ->
        reduce { CounterState() }
        sideEffect(CounterSideEffect.CounterReset)
    }
    
    onDestroy {
        // Optional: cleanup logic
    }
}
```

**Features:**

- Type-safe intent handling using reified types
- Each intent type can have only one handler
- Handlers execute in the context of `ActorScope`
- Less boilerplate code
- Declarative style

**When to use:**

- Small to medium-sized projects
- Simple business logic without complex dependencies
- When you prefer functional style
- Prototyping and quick development

### 3. delegatedActor - Explicit Handler Composition

The `delegatedActor` function allows you to explicitly compose intent handlers, providing maximum flexibility:

```kotlin
val counterActor = delegatedActor<CounterIntent, CounterState, CounterSideEffect>(
    initHandler = InitHandler {
        // Optional: initialization logic
    },
    intentHandlers = listOf(
        intentHandler<CounterIntent, CounterState, CounterSideEffect, CounterIntent.Increment> { intent ->
            reduce { copy(count = count + 1) }
        },
        intentHandler<CounterIntent, CounterState, CounterSideEffect, CounterIntent.Decrement> { intent ->
            reduce { copy(count = count - 1) }
        },
        intentHandler<CounterIntent, CounterState, CounterSideEffect, CounterIntent.Reset> { intent ->
            reduce { CounterState() }
            sideEffect(CounterSideEffect.CounterReset)
        }
    ),
    destroyHandler = DestroyHandler {
        // Optional: cleanup logic
    }
)
```

**Features:**

- Explicit handler registration
- Handlers can be defined separately and composed
- Useful for modular code organization
- Can be combined with code generation (see below)

**When to use:**

- When you need to compose handlers from different modules
- When handlers are defined separately
- When using code generation for intent handlers
- Large projects with complex handler composition

## Code Generation for DelegatedActor

SimpleMVI provides KSP-based code generation to simplify working with `delegatedActor` and intent handlers.

### @DelegatedStore Annotation

Annotate your Store with `@DelegatedStore` to generate type-safe intent handler factories:

```kotlin
@DelegatedStore
class CounterStore : Store<CounterStore.Intent, CounterStore.State, CounterStore.SideEffect> by createStore(
    name = storeName<CounterStore>(),
    initialState = State(counter = 0),
    actor = delegatedActor(
        intentHandlers = listOf(
            incrementIntentHandler(),
            decrementIntentHandler(),
        )
    )
) {
    sealed interface Intent {
        data object Increment : Intent
        data object Decrement : Intent
    }
    
    data class State(val counter: Int)
    
    sealed interface SideEffect {
        data class CounterChanged(val value: Int) : SideEffect
    }
}
```

### Generated Intent Handlers

The annotation processor generates a factory function for creating intent handlers:

```kotlin
// counterStoreIntentHandler is a generated function
fun incrementIntentHandler() = counterStoreIntentHandler<CounterStore.Intent.Increment> { intent ->
    reduce { copy(counter = counter + 1) }
    sideEffect(CounterStore.SideEffect.CounterChanged(counter = state.counter))
}

fun decrementIntentHandler() = counterStoreIntentHandler<CounterStore.Intent.Decrement> { intent ->
    reduce { copy(counter = counter - 1) }
    sideEffect(CounterStore.SideEffect.CounterChanged(counter = state.counter))
}
```

**Benefits:**

- Type-safe intent handler creation
- Less boilerplate code
- Better code organization
- Compile-time safety

**Setup:**

Add the code generation dependency to your build file:

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

## ActorScope

All actor implementations provide access to `ActorScope`, which offers:

```kotlin
interface ActorScope<in Intent : Any, State : Any, in SideEffect : Any> {
    val state: State                          // Current state
    val scope: CoroutineScope                 // Coroutine scope for async work
    
    fun reduce(block: State.() -> State)      // Update state
    fun sideEffect(sideEffect: SideEffect)    // Emit side effect
    fun intent(intent: Intent)                // Dispatch new intent
}
```

For detailed information about ActorScope, see the ActorScope documentation.

## Example: Async Operations

Async operations in actors:

```kotlin
class DataActor : DefaultActor<DataIntent, DataState, DataSideEffect>() {
    
    override fun handleIntent(intent: DataIntent) {
        when (intent) {
            is DataIntent.LoadData -> {
                reduce { copy(loading = true) }
                
                scope.launch {
                    try {
                        val data = repository.fetchData()
                        reduce { copy(loading = false, data = data) }
                        sideEffect(DataSideEffect.DataLoadSucceeded)
                    } catch (e: Exception) {
                        reduce { copy(loading = false, error = e.message) }
                        sideEffect(DataSideEffect.DataLoadFailed(e.message ?: "Unknown error"))
                    }
                }
            }
        }
    }
}
```

## Choosing the Right Approach

| Approach | Best For | Pros | Cons |
|----------|----------|------|------|
| **DefaultActor** | Complex logic, large projects | OOP structure, shared helpers | More boilerplate |
| **actorDsl** | Simple logic, quick development | Concise, declarative | Less structure for complex cases |
| **delegatedActor** | Modular composition, code generation | Flexible handlers | More verbose without codegen |

## Important Notes

- The `Actor` is tightly coupled with the `Store` and should not be used independently.
- Implementations of `Actor` should handle all business logic, state updates, and side effect production based on received intents.
- All coroutines launched in the actor's scope are automatically cancelled when the store is destroyed.
- State updates via `reduce` should be pure functions without side effects.

For more information on how `Actor` interacts with other components, refer to the `Store` and `ActorScope` documentation.