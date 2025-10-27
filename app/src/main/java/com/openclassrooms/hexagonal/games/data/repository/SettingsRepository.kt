package com.openclassrooms.hexagonal.games.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository responsible for managing user settings stored in a [DataStore].
 * This class provides a reactive flow for observing notification preferences
 * and exposes a suspend function to update those preferences.
 *
 * The DataStore ensures persistence of user preferences across app launches.
 *
 * @constructor Injects a [DataStore] instance used to persist key-value settings.
 */
@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    /**
     * Holds preference keys used by [DataStore].
     */
    private object Keys {
        /** Key used to store whether notifications are enabled. */
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    /**
     * A reactive [Flow] that emits the current state of the notification setting.
     * Defaults to `true` if no preference has been set yet.
     */
    val notificationsEnabled: Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[Keys.NOTIFICATIONS_ENABLED] ?: true
        }

    /**
     * Updates the notification preference in [DataStore].
     *
     * @param enabled `true` to enable notifications, `false` to disable them.
     */
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }
}