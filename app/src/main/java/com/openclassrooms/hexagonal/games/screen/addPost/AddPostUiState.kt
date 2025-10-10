package com.openclassrooms.hexagonal.games.screen.addPost

sealed class AddPostUiState {
    object Idle : AddPostUiState() // default state
    object Loading : AddPostUiState() // loading
    object Success : AddPostUiState() // success
    data class Error(val message: String?) : AddPostUiState() // error
}
