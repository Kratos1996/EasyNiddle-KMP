package dev.ishant.easyniddle

import androidx.navigation3.runtime.NavKey

/**
 * Marker interface for application-owned Navigation 3 route keys.
 *
 * Implement this interface with small, immutable data objects or data classes.
 * Annotate every concrete route that can enter a saved back stack with
 * [kotlinx.serialization.Serializable], then register its serializer with
 * [niddleSavedStateConfiguration].
 *
 * Keep UI state, ViewModels, platform objects, and large domain models out of
 * route values; pass stable identifiers and load the data in the destination.
 */
public interface NiddleRoute : NavKey
