package com.arttttt.simplemvi.plugin

import com.arttttt.simplemvi.middleware.Middleware

public class PluginContext<Intent : Any, State : Any, SideEffect : Any>(
    public var initialState: State,
    public val middlewares: MutableList<Middleware<Intent, State, SideEffect>> = mutableListOf(),
)