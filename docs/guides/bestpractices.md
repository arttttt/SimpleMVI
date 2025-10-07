# Best Practices

This guide covers best practices for working with SimpleMVI to build maintainable, testable, and platform-independent applications.

## Domain-Focused Design

### Core Principle

**SimpleMVI is designed for domain logic organization.** The Store should focus on business logic and domain events, not UI implementation details.

### Side Effects: Domain Events, Not UI Actions

Side effects in SimpleMVI represent **domain events** - things that happened in your business logic. They should NOT represent UI actions or instructions.

#### ✅ Good - Domain Events

Domain events describe **what happened** in the business logic:

```kotlin
sealed interface UserSideEffect {
    // Business events
    data object LoginSucceeded : UserSideEffect
    data class LoginFailed(val reason: String) : UserSideEffect
    data object SessionExpired : UserSideEffect
    data class PasswordChanged(val userId: String) : UserSideEffect
}

sealed interface OrderSideEffect {
    // Business outcomes
    data class OrderPlaced(val orderId: String) : OrderSideEffect
    data class PaymentProcessed(val transactionId: String) : OrderSideEffect
    data class OrderFailed(val reason: String) : OrderSideEffect
    data object InventoryInsufficient : OrderSideEffect
}

sealed interface DataSideEffect {
    // Operation results
    data object DataSaveSucceeded : DataSideEffect
    data class DataSaveFailed(val reason: String) : DataSideEffect
    data class ValidationFailed(val errors: List<String>) : DataSideEffect
    data object SyncCompleted : DataSideEffect
}
```

#### ❌ Bad - UI Actions

UI-specific actions tell the UI **what to do**. This violates separation of concerns:

```kotlin
// DON'T DO THIS!
sealed interface UserSideEffect {
    data object NavigateToHome : UserSideEffect           // UI navigation
    data object NavigateBack : UserSideEffect             // UI navigation
    data class ShowToast(val message: String) : UserSideEffect  // UI presentation
    data class ShowError(val message: String) : UserSideEffect  // UI presentation
    data object ShowLoadingDialog : UserSideEffect        // UI state
    data object HideKeyboard : UserSideEffect             // UI action
}
```

### Why Domain Events Matter

#### 1. Platform Independence

Domain events work across all platforms. The same Store can be used on Android, iOS, Desktop, and Web:

```kotlin
// This Store works everywhere
class UserStore : Store<UserIntent, UserState, UserSideEffect> by createStore(
    name = storeName<UserStore>(),
    initialState = UserState.Initial,
    actor = userActor
) {
    sealed interface SideEffect {
        data object LoginSucceeded : SideEffect  // Works on all platforms
        data class LoginFailed(val reason: String) : SideEffect
    }
}
```

#### 2. UI Flexibility

Different platforms can handle the same event differently:

```kotlin
// Android - Navigate with Material Design
LaunchedEffect(Unit) {
    store.sideEffects.collect { sideEffect ->
        when (sideEffect) {
            is UserSideEffect.LoginSucceeded -> {
                navController.navigate("home")
                Toast.makeText(context, "Welcome!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// iOS - Different UI approach for same event
.onReceive(store.sideEffects) { sideEffect in
    switch sideEffect {
    case .loginSucceeded:
        presentationMode.wrappedValue.dismiss()
        showWelcomeAlert = true
    }
}

// Desktop - Another approach
scope.launch {
    store.sideEffects.collect { sideEffect ->
        when (sideEffect) {
            is UserSideEffect.LoginSucceeded -> {
                router.navigateTo(Route.Dashboard)
                notificationService.show("Login successful")
            }
        }
    }
}
```

#### 3. Testability

Domain events are easy to test without UI dependencies:

```kotlin
@Test
fun `login with valid credentials emits LoginSucceeded`() = runTest {
    val store = createUserStore()
    val sideEffects = mutableListOf<UserSideEffect>()
    
    store.sideEffects.onEach { sideEffects.add(it) }.launchIn(this)
    
    store.accept(UserIntent.Login("user@example.com", "password"))
    
    advanceUntilIdle()
    
    assertTrue(sideEffects.contains(UserSideEffect.LoginSucceeded))
}
```

#### 4. Separation of Concerns

Clear boundaries between layers:

```
┌─────────────────────────────────────────┐
│              UI Layer                   │
│  (Platform-specific presentation)       │
│  - Compose, SwiftUI, React              │
│  - Navigation, Toasts, Dialogs          │
│  - Interprets domain events             │
└─────────────────────────────────────────┘
                    ↓
        Observes SideEffects
                    ↓
┌─────────────────────────────────────────┐
│           Domain Layer                  │
│  (Platform-independent logic)           │
│  - Store, Actor, State                  │
│  - Business rules                       │
│  - Emits domain events                  │
└─────────────────────────────────────────┘
```

## Store Organization

### Keep Stores Focused

Each Store should have a single, well-defined responsibility:

```kotlin
// ✅ GOOD - Focused stores
class AuthStore : Store<AuthIntent, AuthState, AuthSideEffect>
class ProfileStore : Store<ProfileIntent, ProfileState, ProfileSideEffect>
class SettingsStore : Store<SettingsIntent, SettingsState, SettingsSideEffect>

// ❌ BAD - God object
class AppStore : Store<AppIntent, AppState, AppSideEffect>  // Everything in one store!
```

### State Structure

Keep state flat and immutable:

```kotlin
// ✅ GOOD - Flat, immutable state
data class UserState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

// ❌ BAD - Nested mutable state
data class UserState(
    var status: Status,  // Mutable!
    val data: MutableMap<String, Any>  // Mutable!
)
```

### Intent Design

Intents should be clear and represent user actions or system events:

```kotlin
// ✅ GOOD - Clear intent names
sealed interface UserIntent {
    data class Login(val email: String, val password: String) : UserIntent
    data object Logout : UserIntent
    data class UpdateProfile(val name: String, val avatar: Uri) : UserIntent
    data object RefreshData : UserIntent
}

// ❌ BAD - Unclear intent names
sealed interface UserIntent {
    data class DoStuff(val data: Any) : UserIntent
    data object Action1 : UserIntent
    data object Action2 : UserIntent
}
```

## Actor Best Practices

### Choose the Right Approach

Use the approach that fits your needs:

- **DefaultActor**: Complex business logic, large projects
- **actorDsl**: Simple logic, quick development
- **delegatedActor**: Modular composition, code generation

### Keep Business Logic in Actors

```kotlin
// ✅ GOOD - Business logic in Actor
class OrderActor : DefaultActor<OrderIntent, OrderState, OrderSideEffect>() {
    override fun handleIntent(intent: OrderIntent) {
        when (intent) {
            is OrderIntent.PlaceOrder -> {
                if (!validateOrder(intent.order)) {
                    sideEffect(OrderSideEffect.ValidationFailed(getErrors()))
                    return
                }
                
                processOrder(intent.order)
            }
        }
    }
    
    private fun validateOrder(order: Order): Boolean {
        // Business validation logic
    }
}

// ❌ BAD - UI logic in Actor
class OrderActor : DefaultActor<OrderIntent, OrderState, OrderSideEffect>() {
    override fun handleIntent(intent: OrderIntent) {
        when (intent) {
            is OrderIntent.PlaceOrder -> {
                // Don't do UI-specific logic here!
                sideEffect(OrderSideEffect.ShowLoadingDialog)
                sideEffect(OrderSideEffect.NavigateToCheckout)
            }
        }
    }
}
```

### Pure State Reducers

Keep `reduce` blocks pure:

```kotlin
// ✅ GOOD - Pure reducer
reduce { 
    copy(
        items = items + newItem,
        totalPrice = calculateTotal(items + newItem)
    )
}

// ❌ BAD - Side effects in reducer
reduce {
    logger.log("Adding item")  // Side effect!
    analytics.track("item_added")  // Side effect!
    copy(items = items + newItem)
}
```

## Error Handling

### Always Handle Errors

```kotlin
// ✅ GOOD - Proper error handling
onIntent<DataIntent.LoadData> { intent ->
    reduce { copy(loading = true, error = null) }
    
    scope.launch {
        try {
            val data = repository.loadData()
            reduce { copy(loading = false, data = data) }
            sideEffect(DataSideEffect.DataLoadSucceeded)
        } catch (e: Exception) {
            reduce { copy(loading = false, error = e.message) }
            sideEffect(DataSideEffect.DataLoadFailed(e.message ?: "Unknown error"))
        }
    }
}

// ❌ BAD - No error handling
onIntent<DataIntent.LoadData> { intent ->
    scope.launch {
        val data = repository.loadData()  // Can crash!
        reduce { copy(data = data) }
    }
}
```

### Specific Error Types

Use specific error types for better handling:

```kotlin
// ✅ GOOD - Specific errors
sealed interface DataSideEffect {
    data class NetworkError(val code: Int) : DataSideEffect
    data class ValidationError(val fields: List<String>) : DataSideEffect
    data object UnauthorizedError : DataSideEffect
    data class UnknownError(val message: String) : DataSideEffect
}

// ❌ BAD - Generic errors
sealed interface DataSideEffect {
    data class Error(val message: String) : DataSideEffect
}
```

## Testing

### Test Store in Isolation

```kotlin
@Test
fun `place order with valid data should succeed`() = runTest {
    val store = createOrderStore()
    val sideEffects = mutableListOf<OrderSideEffect>()
    
    store.sideEffects.onEach { sideEffects.add(it) }.launchIn(this)
    
    val order = createValidOrder()
    store.accept(OrderIntent.PlaceOrder(order))
    
    advanceUntilIdle()
    
    assertEquals(OrderState.Success, store.state)
    assertTrue(sideEffects.any { it is OrderSideEffect.OrderPlaced })
}
```

## Lifecycle Management

### Always Destroy Stores

```kotlin
// ✅ GOOD - Proper cleanup
class MyViewModel : ViewModel() {
    private val store = MyStore()
    
    init {
        store.init()
    }
    
    override fun onCleared() {
        store.destroy()
        super.onCleared()
    }
}

// ❌ BAD - No cleanup
class MyViewModel : ViewModel() {
    private val store = MyStore().apply { init() }
    // No destroy() call - resource leak!
}
```

## Summary

The key principles for working with SimpleMVI:

1. **Domain events, not UI actions** - Side effects represent business events
2. **Platform independence** - Same Store works everywhere
3. **Separation of concerns** - UI interprets, Store decides
4. **Focused stores** - One responsibility per store
5. **Immutable state** - No mutable data structures
6. **Pure reducers** - No side effects in reduce blocks
7. **Proper error handling** - Always handle and report errors
8. **Test everything** - Stores and actors are highly testable
9. **Clean lifecycle** - Always destroy stores
10. **Use the right tool** - Choose the Actor approach that fits your needs