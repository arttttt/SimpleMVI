package com.arttttt.simplemvi.sample.shared.counter

fun decrementIntentHandler() = counterStoreIntentHandler<CounterStore.Intent.Decrement> { intent ->
    reduce {
        copy(
            counter = counter - 1
        )
    }

    sideEffect(CounterStore.SideEffect.CounterChanged(counter = state.counter))
}