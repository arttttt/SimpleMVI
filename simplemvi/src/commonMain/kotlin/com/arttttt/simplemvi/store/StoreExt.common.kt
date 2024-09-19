package com.arttttt.simplemvi.store

operator fun <Intent : Any> Store<Intent, *, *>.plus(intent: Intent) {
    accept(intent)
}

operator fun <Intent : Any> Store<Intent, *, *>.plusAssign(intent: Intent) {
    accept(intent)
}

val <State : Any>Store<*, State, *>.state: State
    get() {
        return states.value
    }