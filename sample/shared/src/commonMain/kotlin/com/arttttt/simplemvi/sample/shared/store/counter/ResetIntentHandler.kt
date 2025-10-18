package com.arttttt.simplemvi.sample.shared.store.counter

fun resetIntentHandler() = counterStoreIntentHandler<CounterStore.Intent.Reset> { intent ->
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