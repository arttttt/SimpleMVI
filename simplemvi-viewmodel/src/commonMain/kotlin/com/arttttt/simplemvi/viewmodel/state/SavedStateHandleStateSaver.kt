package com.arttttt.simplemvi.viewmodel.state

import androidx.lifecycle.SavedStateHandle
import com.arttttt.simplemvi.state.StateSaver
import com.arttttt.simplemvi.state.StateSerializer

public class SavedStateHandleStateSaver<State : Any>(
    private val savedStateHandle: SavedStateHandle,
    private val serializer: StateSerializer<State>,
    private val key: String,
    private val transform: ((state: State) -> State) = { it },
) : StateSaver<State> {

    override fun saveState(state: State) {
        val serializedState = serializer.serialize(state)
        savedStateHandle[key] = serializedState
    }

    override fun restoreState(): State? {
        val serializedState = savedStateHandle.get<String>(key) ?: return null
        val state = serializer.deserialize(serializedState)

        return transform(state)
    }
}