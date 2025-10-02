package com.arttttt.simplemvi.sample.shared.counter

fun incrementHandler() = counterStoreIntentHandler<CounterStore.Intent.Increment> { intent ->
    reduce {
        copy(
            counter = counter + 1
        )
    }

    sideEffect(CounterStore.SideEffect.CounterChanged(counter = state.counter))
}