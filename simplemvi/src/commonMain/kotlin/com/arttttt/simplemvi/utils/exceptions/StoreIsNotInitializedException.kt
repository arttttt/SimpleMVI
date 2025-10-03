package com.arttttt.simplemvi.utils.exceptions

import com.arttttt.simplemvi.store.Store

/**
 * Exception thrown when attempting to use a [Store] that has not been initialized
 *
 * This exception is thrown (in strict mode) or logged (in lenient mode) when:
 * - [Store.accept] is called before [Store.init]
 *
 * @see Store.init
 * @see StoreIsAlreadyDestroyedException
 */
public class StoreIsNotInitializedException : Exception("Attempting to use an uninitialized Store")