package com.arttttt.simplemvi.state

public interface StateSerializer<State : Any> {
    public fun serialize(state: State): String
    public fun deserialize(serializedState: String): State
}