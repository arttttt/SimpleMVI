package com.arttttt.simplemvi.sample.shared.counter

fun resetHandler() = counterStoreIntentHandler<CounterStore.Intent.Reset> { intent ->
    if (state.counter == 0) {
        sideEffect(CounterStore.SideEffect.CantResetCounter)
    } else {
        reduce {
            copy(
                counter = 0
            )
        }

        sideEffect(CounterStore.SideEffect.CounterReset)
    }
}