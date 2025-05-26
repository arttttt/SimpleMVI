package com.arttttt.simplemvi.state

public interface StateSaver<State : Any> {

    public fun saveState(state: State)
    public fun restoreState(): State?
}