package com.arttttt.simplemvi.sample.shared.counter

import com.arttttt.simplemvi.actor.ActorScope
import kotlin.reflect.KClass

class DecrementHandler : CounterStoreIntentHandler<CounterStore.Intent.Decrement> {

    override val intentClass: KClass<CounterStore.Intent.Decrement> = CounterStore.Intent.Decrement::class

    override fun ActorScope<CounterStore.Intent, CounterStore.State, CounterStore.SideEffect>.handle(
        intent: CounterStore.Intent.Decrement
    ) {
        reduce {
            copy(
                counter = counter + 1
            )
        }

        sideEffect(CounterStore.SideEffect.CounterChanged(counter = state.counter))
    }
}