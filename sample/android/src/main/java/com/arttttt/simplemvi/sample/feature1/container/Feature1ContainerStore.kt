package com.arttttt.simplemvi.sample.feature1.container

import com.arttttt.simplemvi.actor.dsl.delegatedActor
import com.arttttt.simplemvi.composition.key
import com.arttttt.simplemvi.composition.scopePlugin
import com.arttttt.simplemvi.sample.feature1.child1.Child1Store
import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.store.createStore
import com.arttttt.simplemvi.store.storeName

class Feature1ContainerStore : Store<Feature1ContainerStore.Intent, Feature1ContainerStore.State, Feature1ContainerStore.SideEffect> by createStore(
    initialize = false,
    name = storeName<Feature1ContainerStore>(),
    initialState = State(
        child1State = Child1Store.State(),
    ),
    actor = delegatedActor(
        intentHandlers = listOf(),
    ),
    plugins = listOf(
        scopePlugin {
            scope(
                key = key<Child1Store.State>(),
                store = Child1Store(),
                updateParentState = { copy(child1State = it) },
                unwrapIntent = { intent -> (intent as? Intent.Child1Intent)?.intent },
                wrapSideEffect = { sideEffect -> SideEffect.Child1SideEffect(sideEffect) },
            )
        }
    ),
) {

    sealed interface Intent {

        data class Child1Intent(
            val intent: Child1Store.Intent,
        ) : Intent
    }

    data class State(
        val child1State: Child1Store.State,
        val navigation: Navigation = Navigation.Child1,
    )

    sealed interface SideEffect {

        data class Child1SideEffect(
            val sideEffect: Child1Store.SideEffect,
        ) : SideEffect
    }
}