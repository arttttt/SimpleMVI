package com.arttttt.simplemvi.actor.dsl

import com.arttttt.simplemvi.actor.Actor

/**
 * DSL marker annotation for [Actor] DSL
 *
 * This annotation is applied to [ActorBuilder] to create a separate DSL scope,
 * preventing accidental nesting of DSL blocks and ensuring proper DSL usage.
 *
 * The [@DslMarker] mechanism helps catch errors at compile-time by preventing
 * calls to DSL methods from outer scopes within inner DSL blocks.
 *
 * Example of what this prevents:
 * ```
 * actorDsl {
 *     onIntent<Intent1> {
 *         onIntent<Intent2> { // Compile error - DSL marker prevents this
 *             // ...
 *         }
 *     }
 * }
 * ```
 *
 * @see ActorBuilder
 * @see actorDsl
 */
@Retention(AnnotationRetention.SOURCE)
@DslMarker
public annotation class ActorDslMarker