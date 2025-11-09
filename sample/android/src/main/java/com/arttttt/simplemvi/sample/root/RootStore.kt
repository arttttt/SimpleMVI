package com.arttttt.simplemvi.sample.root

import com.arttttt.simplemvi.actor.dsl.actorDsl
import com.arttttt.simplemvi.composition.key
import com.arttttt.simplemvi.composition.scopePlugin
import com.arttttt.simplemvi.sample.feature1.container.Feature1ContainerStore
import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.store.createStore
import com.arttttt.simplemvi.store.storeName

class RootStore(
    feature1ContainerStore: Feature1ContainerStore,
) : Store<RootStore.Intent, RootStore.State, RootStore.SideEffect> by createStore(
    name = storeName<RootStore>(),
    initialState = State(
        mode = SampleMode.Unknown,
        feature1State = feature1ContainerStore.state,
    ),
    actor = actorDsl {
        onIntent<Intent.SetClassicMode> {
            reduce {
                copy(mode = SampleMode.Classic)
            }
        }

        onIntent<Intent.SetCompositionMode> {
            reduce {
                copy(mode = SampleMode.Composition)
            }
        }

        onIntent<Intent.SetUnknownMode> {
            reduce {
                copy(mode = SampleMode.Unknown)
            }
        }
    },
    plugins = listOf(
        scopePlugin {
            scope(
                key = key<Feature1ContainerStore.State>(),
                store = feature1ContainerStore,
                updateParentState = { copy(feature1State = it) },
                unwrapIntent = { intent -> (intent as? Intent.Feature1Intent)?.intent },
                wrapSideEffect = { sideEffect -> SideEffect.Feature1SideEffect(sideEffect) },
            )
        }
    ),
) {

    sealed interface Intent {

        data object SetClassicMode : Intent
        data object SetCompositionMode : Intent
        data object SetUnknownMode : Intent

        data class Feature1Intent(val intent: Feature1ContainerStore.Intent) : Intent
    }

    data class State(
        val mode: SampleMode,
        val feature1State: Feature1ContainerStore.State,
    )

    sealed interface SideEffect {

        data class Feature1SideEffect(val sideEffect: Feature1ContainerStore.SideEffect) : SideEffect
    }
}