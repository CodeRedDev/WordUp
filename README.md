# WordUp

[![Build Status](https://travis-ci.com/CodeRedDev/WordUp.svg?branch=master)](https://travis-ci.com/CodeRedDev/WordUp)

WordUp is an Android open source framework for creating soundboard apps.

Currently WordUp exclusively supports `.mp3` audio files that are locally stored (included in the APK).

WordUp offers a range of easy to use utility functions for use cases like:

- Saving sound files from the app to a public directory
- Sharing sounds via WhatsApp, Facebook, Mail or other apps that support file sharing
- Setting sounds as system sound (ringtone, notification or alarm)

## UNDER DEVELOPMENT

While currently under development it should help developers to create their own soundboard app in almost no time.

First steps of this project will be to build a strong local base where sounds will be saved in the apps assets.

Future features should include the ability to host sounds on a server.

## ATTENTION

Be sure to gather information about your current legal situation when publishing a soundboard app.
You might not have the rights to the content you are publishing. 
This framework only provides the tools to create a soundboard app.

## Download

TODO: Add Maven and gradle download instructions

If you plan to use the offered player implementations you have to add Java 8 Support 
to your app's `build.gradle`. Find out how to do that [here](https://developer.android.com/studio/write/java8-support).

## Required initialization

To use WordUp you have to initialize it by a call to `WordUp.init(Context, WordUpConfig)` at your app start.

```kotlin
class WordUpExampleApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
    
        WordUp.init(this, WordUpConfig().apply {
            categoriesEnabled = true
            newSoundsEnabled = true
            directory = resources.getString(R.string.app_name)
        })
    }
}
```

See `WordUpConfig` for the full range of configuration properties.

## How to use

WordUp offers a base to create your own soundboard app. This includes the database structure as well as utilities
for initializing this database or utilities for common actions like sharing, saving or setting sounds as system sound.

See the `example` directory for an example app that uses all of WordUp's API. The example app is designed to
be a strong example of how to write a soundboard app that is user friendly and has a well functioning architecture.
Therefore it takes advantage of dependency injection with [Koin](https://github.com/InsertKoinIO/koin) and uses
the Android MVVM architecture.

### Locally stored sounds

If you prefer to deliver the sound files to your app in the app's APK you should use the `assets` folder
as it will give you the ability to easily initialize your database by iterating through your assets.

You can use the `LocalDbInitializer` to initialize the database without writing your own code.
To do so you will have to structure your sounds depending on two use cases:

##### No categories

- wordup (folder in assets)
  - Sound 1.mp3 (unique)
  - ...

##### Categories

- wordup (folder in assets)
  - Category 1 (subfolder)
    - Sound 1.mp3
    - ...
  - Category 2 (subfolder)
    - Sound 1.mp3 (unique per category)
    - ...
  - ...

### Sharing sounds

For supporting sound sharing you have to define the following provider in your app's manifest.

```xml
<provider
    android:name="de.codereddev.wordup.provider.WordUpProvider"
    android:authorities="${packageName}.wordup.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true" />
```

E.g. you can then share the sound by firing an `Intent` from a fragment like this:

```kotlin
fun shareSound(sound: Sound) {
    val uri = WordUpProvider.getUriForSound(context!!, sound)
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uri)
        type = "audio/mp3"
    }
    context!!.startActivity(Intent.createChooser(shareIntent, getString(R.string.share_sound_via)))
}
```

### Saving sounds OR setting sounds as system sound

The following could be changing on Android 10+.

While you don't have to request any permissions for sharing sounds, saving or setting sounds
as system sound requires permissions (from Android 5.1/6 up to 10).

For saving and setting as system sound

```xml
<uses-permission
    android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="28" />
```

For setting as system sound only

```xml
<uses-permission android:name="android.permission.WRITE_SETTINGS" />
```

Both permissions have to be requested at runtime. For a how-to please see the example app or the Android documentation.

After granted permissions you should be able to call the respective methods from `StorageUtils` without any obvious error.
The methods are suspending functions so you have to call them inside a coroutine.
