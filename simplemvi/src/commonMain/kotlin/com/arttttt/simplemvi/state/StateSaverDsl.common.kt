package com.arttttt.simplemvi.state

import com.arttttt.simplemvi.plugin.PluginsConfigurator
import com.arttttt.simplemvi.store.StoreName

public fun <Intent : Any, State : Any, SideEffect : Any> PluginsConfigurator<Intent, State, SideEffect>.installStateSaverPlugin(
    stateSaverFactory: ((StoreName) -> StateSaver<State>),
) {
    install(StateSavingPlugin(stateSaverFactory))
}