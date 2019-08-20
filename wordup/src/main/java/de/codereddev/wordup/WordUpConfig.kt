package de.codereddev.wordup

import androidx.annotation.DrawableRes

/**
 * A config that holds
 */
class WordUpConfig() {

    var storagePublicDir = DEFAULT_STORAGE_DIR
        private set

    @DrawableRes
    var soundImgResource: Int? = null
        private set
    var soundImgResourceArray: Array<Int>? = null
        private set

    class Builder {

        private var storagePublicDir = DEFAULT_STORAGE_DIR

        @DrawableRes
        private var soundImgResource: Int? = null
        private var soundImgResourceArray: Array<Int>? = null

        /**
         * Sets the user-accessible directory where sounds are saved when the user shares or downloads a sound.
         *
         * @param directory User-accessible directory name.
         */
        fun publicStorageDirectory(directory: String): Builder {
            storagePublicDir = directory
            return this
        }

        /**
         *
         */
        fun withSoundImgResource(@DrawableRes resource: Int): Builder {
            soundImgResource = resource
            return this
        }

        /**
         * Sets a [DrawableRes] array that will be used for random sound button images.
         *
         * If this is set [withSoundImgResource] will be ignored.
         * Pay attention cause there is no verification that the array only contains [DrawableRes].
         */
        fun withSoundImgArray(resourceArray: Array<Int>): Builder {
            soundImgResourceArray = resourceArray
            return this
        }

        fun build(): WordUpConfig {
            val config = WordUpConfig()
            config.storagePublicDir = storagePublicDir
            config.soundImgResource = soundImgResource
            config.soundImgResourceArray = soundImgResourceArray


            return config
        }
    }

    companion object {
        val DEFAULT_CONFIG = WordUpConfig()
        const val DEFAULT_STORAGE_DIR = "WordUp-Soundboard"
    }
}