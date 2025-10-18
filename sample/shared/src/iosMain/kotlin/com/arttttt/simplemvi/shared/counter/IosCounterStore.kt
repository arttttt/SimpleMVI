package com.arttttt.simplemvi.shared.counter

import com.arttttt.simplemvi.logging.logger.com.arttttt.simplemvi.utils.IosStore
import com.arttttt.simplemvi.logging.logger.com.arttttt.simplemvi.utils.asIosStore
import com.arttttt.simplemvi.sample.shared.store.counter.CounterStore

fun CounterStore.asIosStore(): IosStore<CounterStore.Intent, CounterStore.State, CounterStore.SideEffect> {
    return this@asIosStore.asIosStore()
}