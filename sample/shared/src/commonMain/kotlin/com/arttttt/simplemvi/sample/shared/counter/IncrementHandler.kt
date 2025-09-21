package com.arttttt.simplemvi.sample.shared.counter

import com.arttttt.simplemvi.actor.ActorScope
import com.arttttt.simplemvi.actor.delegated.IntentHandler
import kotlin.reflect.KClass

class IncrementHandler : IntentHandler<CounterStore.Intent, CounterStore.State, CounterStore.SideEffect, CounterStore.Intent.Increment> {

    override val intentClass: KClass<CounterStore.Intent.Increment> = CounterStore.Intent.Increment::class

    override fun ActorScope<CounterStore.Intent, CounterStore.State, CounterStore.SideEffect>.handle(
        intent: CounterStore.Intent.Increment
    ) {
        reduce {
            copy(
                counter = counter + 1
            )
        }

        sideEffect(CounterStore.SideEffect.CounterChanged(counter = state.counter))
    }
}