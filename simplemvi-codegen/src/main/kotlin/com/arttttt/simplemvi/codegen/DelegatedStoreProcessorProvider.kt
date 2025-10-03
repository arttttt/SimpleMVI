package com.arttttt.simplemvi.codegen

import com.arttttt.simplemvi.annotations.DelegatedStore
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Provider for [DelegatedStoreProcessor] symbol processor
 *
 * This class is the entry point for KSP (Kotlin Symbol Processing) to discover
 * and instantiate the [DelegatedStoreProcessor]. It's registered via the Java
 * ServiceLoader mechanism in META-INF/services.
 *
 * KSP calls [create] during compilation to obtain an instance of the processor,
 * which then processes classes annotated with [DelegatedStore].
 *
 * @see DelegatedStoreProcessor
 * @see DelegatedStore
 */
class DelegatedStoreProcessorProvider : SymbolProcessorProvider {

    /**
     * Creates a new instance of [DelegatedStoreProcessor]
     *
     * Called by KSP framework during compilation to instantiate the processor.
     * The provided environment gives access to code generation and logging facilities.
     *
     * @param environment The KSP environment providing access to:
     *                   - [SymbolProcessorEnvironment.codeGenerator] for generating source files
     *                   - [SymbolProcessorEnvironment.logger] for logging messages during processing
     * @return A new [DelegatedStoreProcessor] instance configured with the environment
     */
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return DelegatedStoreProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}