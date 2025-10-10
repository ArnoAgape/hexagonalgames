package com.openclassrooms.hexagonal.games.screen.add

sealed class AddPostUiState {
    object Idle : AddPostUiState() // default state
    object Loading : AddPostUiState() // loading
    object Success : AddPostUiState() // added successfully
    data class Error(val message: String?) : AddPostUiState() // error
}
