package com.openclassrooms.hexagonal.games.screen.detailPost

import com.openclassrooms.hexagonal.games.domain.model.Comment

sealed class DetailCommentUiState {
    object Loading : DetailCommentUiState()
    data class Success(val comments: List<Comment>) : DetailCommentUiState()
    sealed class Error(open val message: String) : DetailCommentUiState() {
        data class Empty(override val message: String = "No posts found") : Error(message)
        data class Generic(override val message: String = "Unknown error") : Error(message)
    }
}