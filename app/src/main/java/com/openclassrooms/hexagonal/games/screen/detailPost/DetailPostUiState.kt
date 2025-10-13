package com.openclassrooms.hexagonal.games.screen.detailPost

import com.openclassrooms.hexagonal.games.domain.model.Post

sealed class DetailPostUiState {
    object Loading : DetailPostUiState()
    data class Success(val post: Post) : DetailPostUiState()
    data class Error(val message: String) : DetailPostUiState()
}