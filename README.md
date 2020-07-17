# WordUp

[![Build Status](https://travis-ci.com/CodeRedDev/WordUp.svg?branch=master)](https://travis-ci.com/CodeRedDev/WordUp)

WordUp is an Android open source framework for creating soundboard apps. Continuing we will use the phrase 'word' as a sound item in the soundboard.

Currently WordUp exclusively supports `.mp3` audio files that are locally stored (included in the APK).

WordUp offers a range of easy to use utility functions for use cases like:

- Saving word files from the app to a public directory
- Sharing words via WhatsApp, Facebook, Mail or other apps that support file sharing
- Setting words as system sound (ringtone, notification or alarm)

## UNDER DEVELOPMENT

While currently under development it should help developers to create their own soundboard app in almost no time.

First steps of this project will be to build a strong local base where words will be saved in the apps assets.

Future features should include the ability to host words on a server.

## ATTENTION

Be sure to gather information about your current legal situation when publishing a soundboard app.
You might not have the rights to the content you are publishing. 
This framework only provides the tools to create a soundboard app.

## Download

After adding `mavenCentral()` to your projects `build.gradle` you can add WordUp like this:

```gradle
implementation 'de.codereddev.wordup:wordup-core:0.1.1'
```

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
            newWordsEnabled = true
            directory = resources.getString(R.string.app_name)
        })
    }
}
```

See `WordUpConfig` for the full range of configuration properties.

## How to use

WordUp offers a base to create your own soundboard app. This includes the database structure as well as utilities
for initializing this database or utilities for common actions like sharing, saving or setting words as system sound.

See the `example` directory for an example app that uses all of WordUp's API. The example app is designed to
be a strong example of how to write a soundboard app that is user friendly and has a well functioning architecture.
Therefore it takes advantage of dependency injection with [Koin](https://github.com/InsertKoinIO/koin) and uses
the Android MVVM architecture.

### Locally stored words

If you prefer to deliver the word files to your app in the app's APK you should use the `assets` folder
as it will give you the ability to easily initialize your database by iterating through your assets.

You can use the `LocalDbInitializer` to initialize the database without writing your own code.
To do so you will have to structure your words depending on two use cases:

##### No categories

- wordup (folder in assets)
  - Word 1.mp3 (unique)
  - ...

##### Categories

- wordup (folder in assets)
  - Category 1 (subfolder)
    - Word 1.mp3
    - ...
  - Category 2 (subfolder)
    - Word 1.mp3 (unique per category)
    - ...
  - ...

### Sharing words

For supporting word sharing you have to define the following `FileProvider` in your app's manifest.

```xml
<provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${packageName}.wordup.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
</provider>
```

Where `wordup_paths` is defined like this:

```xml
<paths>
    <cache-path
        name="wordup"
        path="wordup/" />
</paths>
```

You can then share the word by firing an `Intent` from a fragment like this:

```kotlin
fun shareWord(word: Word) {
    val uri = UriUtils.getUriForWord(requireContext(), word)
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uri)
        type = "audio/mp3"
    }
    requireContext().startActivity(Intent.createChooser(shareIntent, getString(R.string.share_word_via)))
}
```

As `UriUtils.getUriForWord(Context, Word)` might do some I/O action it should be called asynchronously.

### Saving words OR setting words as system sound

The following could be changing on Android 10+.

While you don't have to request any permissions for sharing words, saving or setting words
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
As these methods do a lot of I/O work they should be called asynchronously.

## Customization

To give you more control over what kind of data you want to connect to words, instead of using `StandardWordUpDatabase` you can define your own by implementing `WordUpDatabase`.

```kotlin
@Database(entities = [Word::class, Category::class, CustomEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class CustomWordUpDatabase : RoomDatabase(), WordUpDatabase {
    [...]
}
```