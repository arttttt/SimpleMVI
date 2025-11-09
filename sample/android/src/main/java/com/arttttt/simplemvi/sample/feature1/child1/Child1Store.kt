package com.arttttt.simplemvi.sample.feature1.child1

import com.arttttt.simplemvi.actor.dsl.delegatedActor
import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.store.createStore
import com.arttttt.simplemvi.store.storeName

class Child1Store : Store<Child1Store.Intent, Child1Store.State, Child1Store.SideEffect> by createStore(
    name = storeName<Child1Store>(),
    initialState = State(),
    actor = delegatedActor(
        intentHandlers = listOf(),
    )
) {

    sealed interface Intent

    data class State(
        val text: String? = null,
    )

    sealed interface SideEffect
}