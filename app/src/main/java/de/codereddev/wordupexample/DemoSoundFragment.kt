package de.codereddev.wordupexample

import android.os.Bundle
import de.codereddev.wordup.BaseSoundFragment

class DemoSoundFragment : BaseSoundFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        soundPresenter
    }
}