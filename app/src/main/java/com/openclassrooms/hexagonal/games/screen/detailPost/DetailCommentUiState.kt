package com.openclassrooms.hexagonal.games.screen.detailPost

import com.openclassrooms.hexagonal.games.domain.model.Comment

sealed class DetailCommentUiState {
    object Loading : DetailCommentUiState()
    data class Success(val comments: List<Comment>) : DetailCommentUiState()
    data class Error(val message: String) : DetailCommentUiState()
}