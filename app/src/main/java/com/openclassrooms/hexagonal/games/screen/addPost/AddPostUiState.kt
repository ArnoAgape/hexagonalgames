package com.openclassrooms.hexagonal.games.screen.addPost

import com.openclassrooms.hexagonal.games.domain.model.Post

sealed class AddPostUiState {
    object Idle : AddPostUiState()
    object Loading : AddPostUiState()
    data class Success(val post: Post) : AddPostUiState()
    sealed class Error : AddPostUiState() {
        data class NoAccount(val message: String = "No account found") : Error()
        data class Generic(val message: String = "Unknown error") : Error()
    }
}