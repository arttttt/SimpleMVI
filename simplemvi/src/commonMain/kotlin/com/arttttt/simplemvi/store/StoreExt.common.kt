package com.arttttt.simplemvi.store

public operator fun <Intent : Any> Store<Intent, *, *>.plus(intent: Intent) {
    accept(intent)
}

public operator fun <Intent : Any> Store<Intent, *, *>.plusAssign(intent: Intent) {
    accept(intent)
}