package com.openclassrooms.hexagonal.games.screen.detailPost

import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.domain.model.User

sealed class DetailCommentUiState {
    object Loading : DetailCommentUiState()
    data class Success(val comments: List<Comment>, val user: User?) : DetailCommentUiState()
    sealed class Error(open val message: String) : DetailCommentUiState() {
        data class Network(override val message: String = "No internet connection") : Error(message)
        data class Empty(override val message: String = "No posts found") : Error(message)
        data class Generic(override val message: String = "Unknown error") : Error(message)
    }
}