package com.arttttt.simplemvi.plugin

public interface PluginsOwner<Intent : Any, State : Any, SideEffect : Any> {

    public val plugins: List<StorePlugin<Intent, State, SideEffect>>
}