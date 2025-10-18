package com.arttttt.simplemvi.shared.timer

import com.arttttt.simplemvi.logging.logger.com.arttttt.simplemvi.utils.IosStore
import com.arttttt.simplemvi.logging.logger.com.arttttt.simplemvi.utils.asIosStore
import com.arttttt.simplemvi.sample.shared.store.timer.TimerStore

fun TimerStore.asIosStore(): IosStore<TimerStore.Intent, TimerStore.State, TimerStore.SideEffect> {
    return this@asIosStore.asIosStore()
}