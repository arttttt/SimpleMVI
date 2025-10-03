package com.arttttt.simplemvi.utils.exceptions

import com.arttttt.simplemvi.store.Store

/**
 * Exception thrown when attempting to use a [Store] that has been destroyed
 *
 * This exception is thrown (in strict mode) or logged (in lenient mode) when:
 * - [Store.accept] is called after [Store.destroy]
 *
 * Once destroyed, a store cannot be reused and a new instance must be created.
 *
 * @see Store.destroy
 * @see StoreIsNotInitializedException
 */
public class StoreIsAlreadyDestroyedException : Exception("Attempting to use a destroyed Store")