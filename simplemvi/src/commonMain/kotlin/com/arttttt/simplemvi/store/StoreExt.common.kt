package com.arttttt.simplemvi.store

/**
 * a convenient extension for the [Store]
 *
 * it allows you to pass [Intent] like: store + intent
 *
 * @param intent [Intent] to be passed to the [Store]
 *
 * @see Store
 */
public operator fun <Intent : Any> Store<Intent, *, *>.plus(intent: Intent) {
    accept(intent)
}

/**
 * a convenient extension for the [Store]
 *
 * it allows you to pass [Intent] like: store += intent
 *
 * @param intent [Intent] to be passed to the [Store]
 *
 * @see Store
 */
public operator fun <Intent : Any> Store<Intent, *, *>.plusAssign(intent: Intent) {
    accept(intent)
}

public inline fun <reified T : Store<*, *, *>> storeName(): StoreName? {
    return T::class.simpleName?.let(::StoreName)
}