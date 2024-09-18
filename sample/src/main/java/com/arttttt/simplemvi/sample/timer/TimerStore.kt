package com.arttttt.simplemvi.sample.timer

import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.utils.actorDsl
import com.arttttt.simplemvi.utils.createStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class TimerStore(
    coroutineContext: CoroutineContext,
) : Store<TimerStore.Intent, TimerStore.State, TimerStore.SideEffect> by createStore(
    initialState = State(
        isTimerRunning = false,
        value = 0,
    ),
    initialIntents = emptyList(),
    middlewares = listOf(
        TimerMiddleware(),
    ),
    actor = actorDsl(
        coroutineContext = coroutineContext,
    ) {
        var timerJob: Job? = null

        onIntent<Intent.StartTimer> {
            if (timerJob != null) return@onIntent

            timerJob = launch {
                reduce {
                    copy(
                        isTimerRunning = true
                    )
                }

                while (true) {
                    ensureActive()

                    delay(300)

                    reduce {
                        copy(
                            value = state.value + 1
                        )
                    }
                }
            }

            timerJob!!.invokeOnCompletion {
                reduce {
                    copy(
                        isTimerRunning = false
                    )
                }
            }
        }

        onIntent<Intent.StopTimer> {
            timerJob?.cancel()
            timerJob = null
        }

        onIntent<Intent.ResetTimer> {
            reduce {
                copy(
                    value = 0
                )
            }
        }

        onDestroy {
            timerJob?.cancel()
            timerJob = null
        }
    }
) {

    sealed interface Intent {

        data object StartTimer : Intent
        data object StopTimer : Intent
        data object ResetTimer : Intent
    }

    data class State(
        val isTimerRunning: Boolean,
        val value: Int,
    )

    sealed interface SideEffect
}