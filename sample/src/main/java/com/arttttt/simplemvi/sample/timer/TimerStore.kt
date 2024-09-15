package com.arttttt.simplemvi.sample.timer

import android.util.Log
import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.utils.actorDsl
import com.arttttt.simplemvi.utils.createStore
import com.arttttt.simplemvi.utils.loggingActor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class TimerStore(
    coroutineContext: CoroutineContext,
) : Store<TimerStore.Intent, TimerStore.State, TimerStore.SideEffect> by createStore(
    initialState = State(
        value = 0,
    ),
    initialIntents = emptyList(),
    actor = loggingActor(
        name = "TimerStore",
        logger = { message -> Log.e("TimerStore", message) },
        delegate = actorDsl(
            coroutineContext = coroutineContext,
        ) {
            var timerJob: Job? = null

            onIntent<Intent.StartTimer> {
                if (timerJob != null) return@onIntent

                timerJob = launch {
                    while (true) {
                        ensureActive()

                        delay(300)

                        reduce { state ->
                            state.copy(
                                value = state.value + 1
                            )
                        }
                    }
                }
            }

            onIntent<Intent.StopTimer> {
                timerJob?.cancel()
                timerJob = null
            }

            onIntent<Intent.ResetTimer> {
                reduce { state ->
                    state.copy(
                        value = 0
                    )
                }
            }

            onDestroy {
                timerJob?.cancel()
                timerJob = null
            }
        }
    )
) {

    sealed interface Intent {

        data object StartTimer : Intent
        data object StopTimer : Intent
        data object ResetTimer : Intent
    }

    data class State(
        val value: Int,
    )

    sealed interface SideEffect
}