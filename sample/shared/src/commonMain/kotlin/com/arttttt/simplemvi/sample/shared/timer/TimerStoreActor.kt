package com.arttttt.simplemvi.sample.shared.timer

import com.arttttt.simplemvi.actor.DefaultActor
import com.arttttt.simplemvi.sample.shared.timer.TimerStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class TimerStoreActor(
    coroutineContext: CoroutineContext,
) : DefaultActor<TimerStore.Intent, TimerStore.State, TimerStore.SideEffect>(coroutineContext) {

    private var timerJob: Job? = null

    override fun handleIntent(intent: TimerStore.Intent) {
        when (intent) {
            is TimerStore.Intent.StartTimer -> startTimer()
            is TimerStore.Intent.StopTimer -> stopTimer()
            is TimerStore.Intent.ResetTimer -> resetTimer()
        }
    }

    override fun destroy() {
        super.destroy()

        timerJob?.cancel()
        timerJob = null
    }

    private fun startTimer() {
        if (timerJob != null) return

        timerJob = scope.launch {
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

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun resetTimer() {
        reduce {
            copy(
                value = 0
            )
        }
    }

}