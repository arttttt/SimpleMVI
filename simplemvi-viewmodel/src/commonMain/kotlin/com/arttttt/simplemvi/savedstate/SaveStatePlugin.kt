package com.arttttt.simplemvi.savedstate

import com.arttttt.simplemvi.plugin.StorePlugin
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/**
 * State persistence plugin using platform SavedStateHandle.
 */
public class SaveStatePlugin<Intent: Any, State : Any, SideEffect: Any>(
    private val handle: SavedStateHandle,
    private val key: String,
    private val serializer: KSerializer<State>,
    private val json: Json,
) : StorePlugin<Intent, State, SideEffect> {

    override fun provideInitialState(defaultState: State): State {
        val restored = handle.get(key) ?: return defaultState

        return try {
            json.decodeFromString(serializer, restored)
        } catch (_: SerializationException) {
            defaultState
        }
    }

    override fun onStateChanged(oldState: State, newState: State) {
        try {
            val serialized = json.encodeToString(serializer, newState)
            handle.set(key, serialized)
        } catch (e: SerializationException) {
            // Skip saving on serialization error
        }
    }
}

/**
 * Creates SaveStatePlugin with custom key.
 *
 * @param handle platform SavedStateHandle
 * @param key custom storage key
 * @param json Json instance
 */
public inline fun <Intent: Any, reified State : Any, SideEffect: Any> saveStatePlugin(
    handle: SavedStateHandle,
    key: String = buildKey<State>(),
    json: Json = Json.Default,
): SaveStatePlugin<Intent, State, SideEffect> = SaveStatePlugin(
    handle = handle,
    key = key,
    serializer = json.serializersModule.serializer(),
    json = json,
)

@PublishedApi
internal inline fun <reified T : Any> buildKey(): String {
    val baseName = T::class.qualifiedName ?: T::class.simpleName ?: "UnknownState"
    return baseName
}