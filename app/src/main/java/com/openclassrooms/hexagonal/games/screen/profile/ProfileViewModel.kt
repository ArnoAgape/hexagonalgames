package com.openclassrooms.hexagonal.games.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import com.openclassrooms.hexagonal.games.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _user = MutableStateFlow(userRepository.getCurrentUser())
    val user: StateFlow<User?> = _user

    val isSignedIn: Boolean
        get() = userRepository.isUserSignedIn()

    fun signOut() {
        val result = userRepository.signOut()
        if (result.isSuccess) {
            _user.value = null
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val result = userRepository.deleteUser()
            if (result.isSuccess) {
                _user.value = null
            }
        }
    }
}