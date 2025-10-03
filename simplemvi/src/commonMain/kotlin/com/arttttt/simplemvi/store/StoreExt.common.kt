package com.arttttt.simplemvi.store

/**
 * Convenient operator extension for the [Store] that accepts an [Intent]
 *
 * This extension allows using the `+` operator as a more concise syntax
 * for accepting intents instead of calling [Store.accept] directly.
 *
 * Example:
 * ```
 * // Instead of:
 * store.accept(MyIntent.Load)
 *
 * // You can use:
 * store + MyIntent.Load
 * ```
 *
 * @param intent [Intent] to be passed to the [Store]
 *
 * @see Store.accept
 */
public operator fun <Intent : Any> Store<Intent, *, *>.plus(intent: Intent) {
    accept(intent)
}

/**
 * Convenient operator extension for the [Store] that accepts an [Intent]
 *
 * This extension allows using the `+=` operator as an alternative syntax
 * for accepting intents. Functionally equivalent to [plus] operator.
 *
 * Example:
 * ```
 * // Instead of:
 * store.accept(MyIntent.Load)
 *
 * // You can use:
 * store += MyIntent.Load
 * ```
 *
 * @param intent [Intent] to be passed to the [Store]
 *
 * @see Store.accept
 * @see plus
 */
public operator fun <Intent : Any> Store<Intent, *, *>.plusAssign(intent: Intent) {
    accept(intent)
}

/**
 * Helper function to create a [StoreName] from a [Store] class
 *
 * This inline function uses reified type parameters to extract
 * the simple name of the store class and wrap it in [StoreName].
 *
 * Returns null if the class name cannot be determined.
 *
 * @param T The [Store] type to extract the name from
 * @return [StoreName] containing the simple class name, or null if unavailable
 */
public inline fun <reified T : Store<*, *, *>> storeName(): StoreName? {
    return T::class.simpleName?.let(::StoreName)
}