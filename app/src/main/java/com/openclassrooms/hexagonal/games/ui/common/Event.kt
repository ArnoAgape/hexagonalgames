package com.openclassrooms.hexagonal.games.ui.common

import androidx.annotation.StringRes

sealed interface Event {
    data class ShowToast(@StringRes val message: Int): Event
}