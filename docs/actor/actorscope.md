# ActorScope

## Overview

`ActorScope` is the execution context provided to all actors (both `DefaultActor`, `actorDsl`, and `delegatedActor`). It provides access to state, coroutine management, and state modification capabilities.

## Interface Definition

```kotlin
public interface ActorScope<in Intent : Any, State : Any, in SideEffect : Any> {
    val state: State
    val scope: CoroutineScope

    fun intent(intent: Intent)
    fun reduce(block: State.() -> State)
    fun sideEffect(sideEffect: SideEffect)
}
```

## Properties

### state
**Type:** `State` (read-only)

Provides access to the current state of the store:

```kotlin
onIntent<MyIntent.Calculate> { intent ->
    val currentValue = state.value  // Read current state
    val newValue = currentValue * 2
    reduce { copy(value = newValue) }
}
```

**Important characteristics:**

- This property always returns the latest state, even after multiple `reduce` calls within the same intent handler
- The state is read-only; modifications must be done through `reduce`
- Accessing state is synchronous and safe

**Example - Using state for conditional logic:**
```kotlin
onIntent<MyIntent.Decrement> { intent ->
    if (state.counter > 0) {
        reduce { copy(counter = counter - 1) }
    } else {
        sideEffect(MySideEffect.CounterMinimumReached)
    }
}
```

### scope
**Type:** `CoroutineScope`

The coroutine scope tied to the store's lifecycle:

```kotlin
onIntent<MyIntent.LoadData> { intent ->
    reduce { copy(loading = true) }
    
    scope.launch {
        try {
            val data = repository.fetchData()
            reduce { copy(loading = false, data = data) }
        } catch (e: Exception) {
            reduce { copy(loading = false, error = e.message) }
        }
    }
}
```

**Important characteristics:**

- All coroutines launched in this scope are automatically cancelled when the store is destroyed
- The scope uses the `CoroutineContext` provided during store creation
- By default, uses `Dispatchers.Main.immediate`

**Example - Multiple async operations:**
```kotlin
onIntent<MyIntent.LoadProfile> { intent ->
    reduce { copy(loading = true) }
    
    scope.launch {
        try {
            // Launch parallel requests
            val userDeferred = async { userRepository.getUser(intent.userId) }
            val postsDeferred = async { postsRepository.getPosts(intent.userId) }
            
            val user = userDeferred.await()
            val posts = postsDeferred.await()
            
            reduce {
                copy(
                    loading = false,
                    user = user,
                    posts = posts
                )
            }
            sideEffect(MySideEffect.ProfileLoadSucceeded)
        } catch (e: Exception) {
            reduce { copy(loading = false, error = e.message) }
            sideEffect(MySideEffect.ProfileLoadFailed(e.message ?: "Unknown error"))
        }
    }
}
```

## Functions

### reduce

```kotlin
fun reduce(block: State.() -> State)
```

Updates the store's state. The lambda receives the current state and must return a new state:

```kotlin
// Simple state update
reduce { copy(counter = counter + 1) }

// Multiple property updates
reduce { 
    copy(
        counter = counter + 1,
        lastUpdate = System.currentTimeMillis(),
        isModified = true
    )
}

// Conditional updates
reduce {
    if (counter > 0) {
        copy(counter = counter - 1)
    } else {
        this  // Return unchanged state
    }
}
```

**Important rules:**

- State must be immutable (use `data class` with `val` properties)
- Each `reduce` call creates a new state instance
- Middleware is notified of every state change
- The lambda should be a pure function without side effects

**Example - Complex state transformation:**
```kotlin
onIntent<MyIntent.UpdateUser> { intent ->
    reduce {
        copy(
            user = user.copy(
                name = intent.newName,
                email = intent.newEmail,
                updatedAt = Clock.System.now()
            ),
            isDirty = true
        )
    }
}
```

**Anti-pattern - Side effects in reduce:**
```kotlin
// ❌ BAD - Don't do this
reduce { 
    logEvent("state_changed")  // Side effect!
    copy(value = value + 1)
}

// ✅ GOOD - Keep reduce pure
reduce { copy(value = value + 1) }
logEvent("state_changed")
```

### sideEffect

```kotlin
fun sideEffect(sideEffect: SideEffect)
```

Emits a side effect from the actor. Side effects represent one-time events that cannot be represented in state:

```kotlin
onIntent<MyIntent.SaveData> { intent ->
    reduce { copy(saving = true) }
    
    scope.launch {
        val result = repository.save(intent.data)
        
        if (result.isSuccess) {
            reduce { copy(saving = false) }
            sideEffect(DataSideEffect.DataSaveSucceeded)
        } else {
            reduce { copy(saving = false, error = result.error) }
            sideEffect(DataSideEffect.DataSaveFailed(result.error))
        }
    }
}
```

**Example side effects:**
```kotlin
sealed interface DataSideEffect {
    data object DataSaveSucceeded : DataSideEffect
    data class DataSaveFailed(val reason: String) : DataSideEffect
    data class ValidationFailed(val errors: List<String>) : DataSideEffect
    data object SessionExpired : DataSideEffect
}
```

### intent

```kotlin
fun intent(intent: Intent)
```

Dispatches a new intent to be processed by the actor. Useful for creating multi-step workflows:

```kotlin
onIntent<MyIntent.StartProcess> { intent ->
    reduce { copy(step = 1, status = "Processing step 1") }
    // Process step 1...
    
    // Move to next step
    intent(MyIntent.ProcessStep2)
}

onIntent<MyIntent.ProcessStep2> { intent ->
    reduce { copy(step = 2, status = "Processing step 2") }
    // Process step 2...
    
    intent(MyIntent.ProcessStep3)
}

onIntent<MyIntent.ProcessStep3> { intent ->
    reduce { copy(step = 3, status = "Process complete") }
    sideEffect(MySideEffect.ProcessCompleted)
}
```

**Use cases:**

- **Splitting complex logic** into multiple steps
- **Creating intent chains** for sequential operations
- **Conditional workflows** based on state or results

**Example - Conditional intent dispatching:**
```kotlin
onIntent<MyIntent.CheckAndLoad> { intent ->
    if (state.isAuthenticated) {
        intent(MyIntent.LoadUserData)
    } else {
        sideEffect(MySideEffect.AuthenticationRequired)
    }
}
```

**Warning:** Be careful not to create infinite loops:
```kotlin
// ❌ BAD - Infinite loop!
onIntent<MyIntent.BadIntent> { intent ->
    intent(MyIntent.BadIntent)  // Calls itself!
}

// ✅ GOOD - Use conditions to break loops
onIntent<MyIntent.Retry> { intent ->
    if (state.retryCount < 3) {
        reduce { copy(retryCount = retryCount + 1) }
        scope.launch {
            // Retry logic
        }
    } else {
        sideEffect(MySideEffect.MaxRetriesReached)
    }
}
```