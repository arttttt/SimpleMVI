# Middleware

The `Middleware` is designed to provide a way to "spy" on the `Store` without modifying its behavior. It's useful for implementing cross-cutting concerns such as logging, analytics, or debugging.

## Overview

`Middleware` allows you to:

- Observe `Intent` received by the `Store`
- Monitor `State` changes
- Track `SideEffect` production

It's important to note that `Middleware` cannot modify the input events it observes.

## Interface Definition

```kotlin
public interface Middleware<Intent : Any, State : Any, SideEffect : Any> {
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
}
```

## Key Methods

### onIntent

```kotlin
public fun onIntent(intent: Intent, state: State)
```

This method is invoked when the `Store` receives a new `Intent`. It provides access to:
- The received `Intent`
- The current `State` at the time the `Intent` was received

### onStateChanged

```kotlin
public fun onStateChanged(oldState: State, newState: State)
```

This method is called when a new `State` is produced inside the `Actor`. It provides access to:
- The previous `State`
- The new `State`

This allows for easy comparison and tracking of state changes.

### onSideEffect

```kotlin
public fun onSideEffect(sideEffect: SideEffect, state: State)
```

This method is invoked when the `Actor` produces a new `SideEffect`. It provides access to:
- The produced `SideEffect`
- The current `State` at the time the `SideEffect` was produced

## Use Cases

`Middleware` can be used for various purposes, including but not limited to:
- Logging: Track all events passing through the `Store` for debugging or auditing purposes.
- Analytics: Capture user actions and state changes for analysis.
- Debugging: Monitor the flow of `Intent`, `State`, and `SideEffect` for troubleshooting.
- Performance Monitoring: Track the frequency and timing of state changes and side effects.

## Important Notes

- `Middleware` is a passive observer and cannot modify the events it receives.
- Multiple `Middleware` instances can be used simultaneously in a single `Store`.
- `Middleware` methods should be implemented efficiently to avoid impacting the performance of the main application logic.
- While `Middleware` has access to the `State`, it should not attempt to modify it directly.

For more information on how `Middleware` interacts with other components, refer to the `Store` and `Actor` documentation.
