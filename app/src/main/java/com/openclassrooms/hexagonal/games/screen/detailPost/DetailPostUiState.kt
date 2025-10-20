package com.openclassrooms.hexagonal.games.screen.detailPost

import com.openclassrooms.hexagonal.games.domain.model.Post

sealed class DetailPostUiState {
    object Loading : DetailPostUiState()
    data class Success(val post: Post) : DetailPostUiState()
    sealed class Error : DetailPostUiState() {
        data class Network(val message: String = "No internet connection") : Error()
        data class Empty(val message: String = "No posts found") : Error()
        data class Generic(val message: String = "Unknown error") : Error()
    }
}