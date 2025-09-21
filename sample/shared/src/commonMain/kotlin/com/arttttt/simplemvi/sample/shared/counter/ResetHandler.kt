package com.arttttt.simplemvi.sample.shared.counter

import com.arttttt.simplemvi.actor.ActorScope
import com.arttttt.simplemvi.actor.delegated.IntentHandler
import com.arttttt.simplemvi.sample.shared.counter.CounterStore.SideEffect
import kotlin.reflect.KClass

class ResetHandler : IntentHandler<CounterStore.Intent, CounterStore.State, CounterStore.SideEffect, CounterStore.Intent.Reset> {

    override val intentClass: KClass<CounterStore.Intent.Reset> = CounterStore.Intent.Reset::class

    override fun ActorScope<CounterStore.Intent, CounterStore.State, CounterStore.SideEffect>.handle(
        intent: CounterStore.Intent.Reset
    ) {
        if (state.counter == 0) {
            sideEffect(SideEffect.CantResetCounter)
        } else {
            reduce {
                copy(
                    counter = 0
                )
            }

            sideEffect(SideEffect.CounterReset)
        }
    }
}