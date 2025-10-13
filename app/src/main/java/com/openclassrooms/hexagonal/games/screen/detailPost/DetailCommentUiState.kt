package com.openclassrooms.hexagonal.games.screen.detailPost

import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.domain.model.Post

sealed class DetailCommentUiState {
    object Loading : DetailCommentUiState()
    data class Success(val comment: Comment) : DetailCommentUiState()
    data class Error(val message: String) : DetailCommentUiState()
}