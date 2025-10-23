package com.openclassrooms.hexagonal.games.screen.addComment

import com.openclassrooms.hexagonal.games.domain.model.Comment

sealed class AddCommentUiState {
    object Idle : AddCommentUiState()
    object Loading : AddCommentUiState()
    data class Success(val comment: Comment) : AddCommentUiState()
    sealed class Error : AddCommentUiState() {
        data class NoAccount(val message: String = "No account found") : Error()
        data class Generic(val message: String = "Unknown error") : Error()
    }
}