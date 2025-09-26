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

    private val _user = MutableStateFlow<User?>(userRepository.getCurrentUser())
    val user: StateFlow<User?> = _user

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            val result = userRepository.signInWithEmail(email, password)
            result.onSuccess { _user.value = it }
        }
    }

    fun signOut() {
        userRepository.signOut()
        _user.value = null
    }
}