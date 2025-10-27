package com.openclassrooms.hexagonal.games.ui.common

import androidx.annotation.StringRes

/**
 * Represents one-time UI events used to communicate from ViewModels to the UI layer.
 *
 * This sealed interface defines events that are not part of the persistent state
 * (e.g., navigation, toast messages, snack bars).
 *
 * Currently, it supports:
 * - [ShowToast]: Displays a short message using a string resource.
 */
sealed interface Event {

    /**
     * Event used to display a toast message to the user.
     *
     * @param message The string resource ID of the message to display.
     */
    data class ShowToast(@StringRes val message: Int) : Event
}
