package com.arttttt.simplemvi.plugin

public class PluginsConfigurator<Intent: Any, State: Any, SideEffect: Any> {
    internal val installedPlugins = mutableListOf<StorePlugin<Intent, State, SideEffect>>()

    public fun install(plugin: StorePlugin<Intent, State, SideEffect>) {
        installedPlugins.add(plugin)
    }
}