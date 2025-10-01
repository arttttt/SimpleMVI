package com.arttttt.simplemvi.codegen

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class DelegatedStoreProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return DelegatedStoreProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}