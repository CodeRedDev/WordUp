package de.codereddev.wordup

import de.codereddev.wordup.provider.WordUpProvider

/**
 * A configuration class to configure the behaviour of WordUp.
 */
class WordUpConfig {

    /**
     * Defines if sharing of sounds is enabled.
     *
     * If this is true [WordUp] will throw an exception
     * if no [WordUpProvider] is defined in manifest.
     */
    var sharingEnabled = true

    /**
     * Defines if the sounds should be categorized.
     *
     * If this is true it will force you to organize your
     * assets and network resources to support categories.
     */
    var categoriesEnabled: Boolean = false

    /**
     * Defines the behaviour of database intializers offered by WordUp.
     */
    var newSoundsEnabled: Boolean = true

    /**
     * Defines a directory name for storing sounds.
     */
    var directory: String? = null
}
