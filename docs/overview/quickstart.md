## Modifying build script

Add a new dependency to the gradle build script

=== "Kotlin"

    ``` kotlin
    implementation("io.github.arttttt.simplemvi:simplemvi:<version>")
    ```
=== "Groovy"

    ``` groovy
    implementation "io.github.arttttt.simplemvi:simplemvi:<version>"
    ```

## Creating Store

This easiest way to create a `Store` is declaring an object which impletents the `Store` interface

```kotlin
class MyCoolStore : Store<MyCoolStore.Intent, MyCoolStore.State, MyCoolStore.SideEffect> {

    sealed interface Intent

    data class State(val value: Int = 0)

    sealed interface SideEffect
}
```

Now it's necessary to implement the `Store` interface itself. It can be done by using the `createStore` function

```kotlin
class MyCoolStore : Store</* omitted code */> by createStore(
    initialState = //Store initial state,
    actor = //Store actor,
) {

    // Omitted code
}
```

There are only two mandatory parameters that need to be provided: `initialState` and `actor`

`initialState` - initial state of the `Store`. A brand new instance of the `Store` is created with this provided state

`actor` - a place where all business logic is placed

## Creating Actor

`Actor` can be created by using dsl

```kotlin
class MyCoolStore : Store</* omitted code */> by createStore(
    initialState = //Store initial state,
    actor = actorDsl {
        init { /* code */ }

        onIntent<Intent.DoCoolStuff> { intent -> /* code */ }

        onDestroy { /* code */ }
    },
) {
    sealed interface Intent {
        data object DoCoolStuff : Intent
    }

    // Omitted code
}
```

`init` - called when the `Store` initialized

`onIntent<T>` - called when a declared intent received

`onDestroy` - called when store is about to be destroyed

That's basically it!