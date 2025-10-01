package com.arttttt.simplemvi.sample.shared.counter

import com.arttttt.simplemvi.actor.ActorScope
import kotlin.reflect.KClass

class ResetHandler : CounterStoreIntentHandler<CounterStore.Intent.Reset> {

    override val intentClass: KClass<CounterStore.Intent.Reset> = CounterStore.Intent.Reset::class

    override fun ActorScope<CounterStore.Intent, CounterStore.State, CounterStore.SideEffect>.handle(
        intent: CounterStore.Intent.Reset
    ) {
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
}