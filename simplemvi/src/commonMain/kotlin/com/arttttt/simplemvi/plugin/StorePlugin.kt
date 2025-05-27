package com.arttttt.simplemvi.plugin

import com.arttttt.simplemvi.store.StoreName

public fun interface StorePlugin<Intent : Any, State : Any, SideEffect : Any> {

    public fun install(
        name: StoreName?,
        context: PluginContext<Intent, State, SideEffect>,
    )
}