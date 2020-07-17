package de.codereddev.wordup

import androidx.core.content.FileProvider

/**
 * A configuration class to configure the behaviour of WordUp.
 */
class WordUpConfig {

    /**
     * Defines if sharing of words is enabled.
     *
     * If this is true [WordUp] will throw an exception
     * if no [FileProvider] is defined in manifest.
     */
    var sharingEnabled = true

    /**
     * Defines if the words should be categorized.
     *
     * If this is true it will force you to organize your
     * assets and network resources to support categories.
     */
    var categoriesEnabled: Boolean = false

    /**
     * Defines the behaviour of database intializers offered by WordUp.
     */
    var newWordsEnabled: Boolean = true

    /**
     * Defines a directory name for storing words.
     */
    var directory: String? = null
}
