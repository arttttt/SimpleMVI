package com.arttttt.simplemvi.codegen

import com.arttttt.simplemvi.annotations.TCAFeature
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Provider for [TCAFeatureProcessor] symbol processor
 *
 * This class is the entry point for KSP (Kotlin Symbol Processing) to discover
 * and instantiate the [TCAFeatureProcessor]. It's registered via the Java
 * ServiceLoader mechanism in META-INF/services.
 *
 * KSP calls [create] during compilation to obtain an instance of the processor,
 * which then processes classes annotated with [TCAFeature] and generates
 * corresponding Swift TCA Feature code.
 *
 * @see TCAFeatureProcessor
 * @see TCAFeature
 */
class TCAFeatureProcessorProvider : SymbolProcessorProvider {

    /**
     * Creates a new instance of [TCAFeatureProcessor]
     *
     * Called by KSP framework during compilation to instantiate the processor.
     * The provided environment gives access to code generation and logging facilities.
     *
     * @param environment The KSP environment providing access to:
     *                   - [SymbolProcessorEnvironment.codeGenerator] for generating Swift source files
     *                   - [SymbolProcessorEnvironment.logger] for logging messages during processing
     * @return A new [TCAFeatureProcessor] instance configured with the environment
     */
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return TCAFeatureProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
        )
    }
}