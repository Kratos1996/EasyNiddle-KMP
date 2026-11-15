package dev.ishant.easyniddle

/**
 * Familiar EasyNiddle route marker retained for source-level migration.
 *
 * This is a multiplatform alias of [NiddleRoute]; it does not use Java
 * serialization. Annotate each concrete route with kotlinx.serialization's
 * [kotlinx.serialization.Serializable] and register its serializer when
 * configuring a saveable back stack.
 */
public interface SerializableRoute : NiddleRoute
