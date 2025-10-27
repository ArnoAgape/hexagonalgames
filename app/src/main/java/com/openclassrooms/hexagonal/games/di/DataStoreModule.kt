package com.openclassrooms.hexagonal.games.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Extension property that provides a singleton instance of [DataStore]
 * for storing user preferences.
 *
 * This DataStore uses the name `"user_settings"` and is backed by
 * [androidx.datastore.preferences.core.Preferences].
 *
 * It allows the app to persist lightweight configuration data such as
 * notification preferences or user settings across app launches.
 */
private val Context.dataStore by preferencesDataStore(name = "user_settings")

/**
 * Dagger-Hilt module that provides a singleton instance of [DataStore].
 *
 * This module installs into the [SingletonComponent], ensuring that
 * the same DataStore instance is used throughout the application.
 *
 * @see provideDataStore
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    /**
     * Provides a [DataStore] instance for persisting user preferences.
     *
     * @param context The application context injected by Hilt.
     * @return A [DataStore] configured to store [Preferences] under the `"user_settings"` name.
     */
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}