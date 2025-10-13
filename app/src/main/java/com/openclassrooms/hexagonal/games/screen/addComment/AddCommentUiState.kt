package com.openclassrooms.hexagonal.games.screen.addComment

sealed class AddCommentUiState {
    object Idle : AddCommentUiState()
    object Loading : AddCommentUiState()
    object Success : AddCommentUiState()
    data class Error(val message: String?) : AddCommentUiState()
}