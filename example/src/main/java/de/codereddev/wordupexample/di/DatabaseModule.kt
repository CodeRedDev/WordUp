package de.codereddev.wordupexample.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.codereddev.wordup.database.CategoryDao
import de.codereddev.wordup.database.StandardWordUpDatabase
import de.codereddev.wordup.database.WordDao
import de.codereddev.wordup.database.WordUpDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(application: Application): WordUpDatabase {
        return StandardWordUpDatabase.getInstance(application)
    }

    @Provides
    fun provideWordDao(db: WordUpDatabase): WordDao {
        return db.getWordDao()
    }

    @Provides
    fun provideCategoryDao(db: WordUpDatabase): CategoryDao {
        return db.getCategoryDao()
    }
}
