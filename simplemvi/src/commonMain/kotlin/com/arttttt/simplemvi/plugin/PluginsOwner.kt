package com.arttttt.simplemvi.plugin

@PublishedApi
internal interface PluginsOwner<Intent : Any, State : Any, SideEffect : Any> {

    val plugins: List<StorePlugin<Intent, State, SideEffect>>
}