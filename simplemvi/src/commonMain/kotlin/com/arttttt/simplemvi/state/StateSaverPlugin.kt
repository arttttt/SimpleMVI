package com.arttttt.simplemvi.state

import com.arttttt.simplemvi.plugin.PluginContext
import com.arttttt.simplemvi.plugin.StorePlugin
import com.arttttt.simplemvi.store.StoreName

public class StateSavingPlugin<Intent : Any, State : Any, SideEffect : Any>(
    private val stateSaverFactory: ((StoreName) -> StateSaver<State>),
) : StorePlugin<Intent, State, SideEffect> {

    override fun install(
        name: StoreName?,
        context: PluginContext<Intent, State, SideEffect>,
    ) {
        name ?: return

        val stateSaver = stateSaverFactory(name)

        stateSaver.restoreState()?.let { restoredState ->
            context.initialState = restoredState
        }

        context.middlewares.add(StateSaverMiddleware(stateSaver))
    }
}