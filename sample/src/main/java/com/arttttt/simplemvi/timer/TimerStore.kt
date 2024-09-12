package com.arttttt.simplemvi.timer

import android.util.Log
import com.arttttt.simplemvi.Store
import com.arttttt.simplemvi.actorDsl
import com.arttttt.simplemvi.createStore
import com.arttttt.simplemvi.loggingActor
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
    actor = loggingActor(
        name = "TimerStore",
        logger = { message -> Log.e("TimerStore", message) },
        delegate = actorDsl(
            coroutineContext = coroutineContext,
        ) {
            var timerJob: Job? = null

            onIntent<Intent.StartTimer> {
                if (timerJob != null) return@onIntent

                timerJob = scope.launch {
                    while (true) {
                        ensureActive()

                        delay(1000)

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