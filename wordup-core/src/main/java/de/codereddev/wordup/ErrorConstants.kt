package de.codereddev.wordup

internal object ErrorConstants {

    const val CONFIG_NOT_DEFINED = "WordUp not configured!"
    const val CONFIG_DIRECTORY_NOT_DEFINED = "A directory must be set in WordUpConfig!"

    const val FILE_PROVIDER_MISSING = "FileProvider definition in manifest missing or incorrect!"

    const val STORAGE_ASSET_FD =
        "AssetFileDescriptor can only be retrieved from a locally stored word!"
    const val STORAGE_ASSET_INPUT_STREAM =
        "InputStream for asset can only be retrieved from a locally stored word!"
    const val STORAGE_MEDIASTORE_INSERTING = "Failed to insert word into MediaStore."
    const val STORAGE_SYSTEM_SOUND_OPTION_EMPTY = "No system sound option given."
    const val STORAGE_SYSTEM_SOUND_OPTION =
        "The given system sound option is not valid - The following options are supported: " +
                "MediaStore.Audio.Media.IS_RINGTONE, *.IS_NOTIFICATION, *.IS_ALARM"

    const val INITIALIZER_NO_CATEGORY_SUBFOLDER =
        "With the given WordUpConfig category subfolders are not supported!"
    const val INITIALIZER_CATEGORY_ROOT_MP3 =
        "With the given WordUpConfig word files must be located in a category subfolder!"
    const val INITIALIZER_CATEGORY_SUBFOLDER =
        "Subfolders are not allowed in category subfolders!"
    const val INITIALIZER_WORDUP_ASSET = "Unknown error at WordUp asset folder. (NullPointer)"

    const val PLAYER_LOCAL_NO_LOCAL = "LocalWordUpPlayer only supports local resources!"
}
