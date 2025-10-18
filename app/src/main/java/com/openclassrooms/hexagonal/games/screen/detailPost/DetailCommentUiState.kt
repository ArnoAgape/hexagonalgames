package com.openclassrooms.hexagonal.games.screen.detailPost

import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.domain.model.User

sealed class DetailCommentUiState {
    object Loading : DetailCommentUiState()
    data class Success(val comments: List<Comment>, val user: User?) : DetailCommentUiState()
    data class Error(val message: String) : DetailCommentUiState()
}