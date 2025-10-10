package com.openclassrooms.hexagonal.games.screen.detailPost

import com.openclassrooms.hexagonal.games.domain.model.Post

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val post: Post) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}