package com.arttttt.simplemvi.logging

import com.arttttt.simplemvi.logging.logger.Logger
import com.arttttt.simplemvi.plugin.PluginContext
import com.arttttt.simplemvi.plugin.StorePlugin
import com.arttttt.simplemvi.store.StoreName

public class LoggingPlugin<Intent : Any, State : Any, SideEffect : Any>(
    private val logger: Logger,
) : StorePlugin<Intent, State, SideEffect> {

    override fun install(
        name: StoreName?,
        context: PluginContext<Intent, State, SideEffect>
    ) {
        name ?: return

        context.middlewares.add(LoggingMiddleware(name.name, logger))
    }
}