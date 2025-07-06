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

## Important Notes

- The `Actor` is tightly coupled with the `Store` and should not be used independently.
- Implementations of `Actor` should handle all business logic, state updates, and side effect production based on received intents.

For more information on how `Actor` interacts with other components, refer to the `Store` documentation.
