package com.openclassrooms.hexagonal.games.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import com.openclassrooms.hexagonal.games.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing and exposing user profile data.
 *
 * It observes the currently authenticated user through [UserRepository],
 * allows syncing user data with Firestore, and provides sign-out and
 * account deletion operations.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    /** Backing state for the current user profile. */
    private val _user = MutableStateFlow<User?>(null)

    /** Exposed immutable flow representing the currently signed-in user. */
    val user: StateFlow<User?> = _user.asStateFlow()

    /**
     * Observes the current user from [UserRepository] and updates the [_user] state.
     * Called automatically when the ViewModel is initialized.
     */
    init {
        viewModelScope.launch {
            userRepository.observeCurrentUser()
                .collect { user ->
                    _user.value = user
                }
        }
    }

    /**
     * Ensures the authenticated user is present in Firestore.
     * This can be used to synchronize user data after login or profile updates.
     */
    fun syncUserWithFirestore() {
        viewModelScope.launch {
            userRepository.ensureUserInFirestore()
        }
    }

    /**
     * Observes whether a user is currently signed in.
     * Exposed as a [StateFlow] for reactive UI updates.
     */
    val isSignedIn =
        userRepository.isUserSignedIn()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = false
            )

    /**
     * Signs the user out using [UserRepository.signOut].
     * If successful, the local [_user] state is reset to null.
     */
    fun signOut() {
        viewModelScope.launch {
            val result = userRepository.signOut()
            if (result.isSuccess) {
                _user.value = null
            }
        }
    }

    /**
     * Deletes the currently authenticated user's account and associated Firestore data.
     * If successful, clears the local user state.
     */
    fun deleteAccount() {
        viewModelScope.launch {
            val result = userRepository.deleteUser()
            if (result.isSuccess) {
                _user.value = null
            }
        }
    }
}