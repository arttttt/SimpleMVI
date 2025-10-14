package com.arttttt.simplemvi.logging.logger.com.arttttt.simplemvi.utils

import com.arttttt.simplemvi.store.Store

public class IosStore<Intent : Any, State : Any, SideEffect : Any> internal constructor(
    private val delegate: Store<Intent, State, SideEffect>,
) {

    public val state: State
        get() = delegate.state

    public val states: CStateFlow<State> = CStateFlow(delegate.states)

    public val sideEffects: CFlow<SideEffect> = CFlow(delegate.sideEffects)

    public fun init() {
        delegate.init()
    }

    public fun accept(intent: Intent) {
        delegate.accept(intent)
    }

    public fun destroy() {
        delegate.destroy()
    }
}

public fun <Intent : Any, State : Any, SideEffect : Any> Store<Intent, State, SideEffect>.asIosStore(): IosStore<Intent, State, SideEffect> {
    return IosStore(this)
}