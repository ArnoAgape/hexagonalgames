package com.openclassrooms.hexagonal.games.screen.homefeed

import com.openclassrooms.hexagonal.games.domain.model.Post

sealed class PostUiState {
    object Loading : PostUiState()
    data class Success(val posts: List<Post>) : PostUiState()
    sealed class Error : PostUiState() {
        data class Empty(val message: String = "No posts found") : Error()
        data class Generic(val message: String = "Unknown error") : Error()
    }
}