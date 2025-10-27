package com.openclassrooms.hexagonal.games.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.openclassrooms.hexagonal.games.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing user notification settings.
 *
 * It exposes a reactive [kotlinx.coroutines.flow.StateFlow] representing whether notifications are enabled
 * and allows toggling this preference. It also manages Firebase Cloud Messaging (FCM)
 * topic subscriptions to enable or disable global notifications.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    /** Reactive stream indicating whether notifications are enabled. */
    val notificationsEnabled = settingsRepository.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    /**
     * Toggles the user's notification preference.
     *
     * Updates the stored value in [SettingsRepository] and subscribes or unsubscribes
     * from the `"global"` Firebase Cloud Messaging topic based on the new state.
     *
     * @param enabled `true` to enable notifications and subscribe to FCM,
     * `false` to disable notifications and unsubscribe.
     */
    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
            val fcm = FirebaseMessaging.getInstance()
            if (enabled) {
                fcm.subscribeToTopic("global")
            } else {
                fcm.unsubscribeFromTopic("global")
            }
        }
    }
}