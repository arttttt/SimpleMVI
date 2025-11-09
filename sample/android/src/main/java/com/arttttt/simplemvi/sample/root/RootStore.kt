package com.arttttt.simplemvi.sample.root

import com.arttttt.simplemvi.actor.dsl.actorDsl
import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.store.createStore
import com.arttttt.simplemvi.store.storeName

class RootStore : Store<RootStore.Intent, RootStore.State, RootStore.SideEffect> by createStore(
    name = storeName<RootStore>(),
    initialState = State(
        mode = SampleMode.Unknown,
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
) {

    sealed interface Intent {

        data object SetClassicMode : Intent
        data object SetCompositionMode : Intent
        data object SetUnknownMode : Intent
    }

    data class State(
        val mode: SampleMode,
    )

    sealed interface SideEffect
}