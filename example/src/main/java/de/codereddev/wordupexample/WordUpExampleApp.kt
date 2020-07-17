package de.codereddev.wordupexample

import android.app.Application
import de.codereddev.wordup.WordUp
import de.codereddev.wordup.WordUpConfig
import de.codereddev.wordup.model.database.WordUpDatabase
import de.codereddev.wordupexample.viewmodel.WordListViewModel
import de.codereddev.wordupexample.viewmodel.WordListViewModelImpl
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class WordUpExampleApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@WordUpExampleApp)
            modules(applicationModule)
        }

        WordUp.init(this, WordUpConfig().apply {
            categoriesEnabled = true
            newWordsEnabled = true
            directory = resources.getString(R.string.app_name)
        })
    }

    private val applicationModule = module {

        single {
            WordUpDatabase.getInstance(get())
        }

        single {
            get<WordUpDatabase>().wordDao()
        }

        single {
            get<WordUpDatabase>().categoryDao()
        }

        viewModel<WordListViewModel> {
            WordListViewModelImpl(get(), get())
        }
    }
}
