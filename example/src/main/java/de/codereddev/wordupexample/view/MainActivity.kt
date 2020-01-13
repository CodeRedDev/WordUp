package de.codereddev.wordupexample.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.codereddev.wordupexample.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SoundListFragment())
            .commit()
    }
}
