package com.arttttt.simplemvi.state

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

public class KotlinSerializationStateSerializer<State : Any>(
    private val serializer: KSerializer<State>
) : StateSerializer<State> {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    override fun serialize(state: State): String {
        return json.encodeToString(serializer, state)
    }

    override fun deserialize(serializedState: String): State {
        return json.decodeFromString(serializer, serializedState)
    }
}