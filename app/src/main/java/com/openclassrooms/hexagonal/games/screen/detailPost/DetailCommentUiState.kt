package com.openclassrooms.hexagonal.games.screen.detailPost

import com.openclassrooms.hexagonal.games.domain.model.Comment

/**
 * Represents the various UI states for displaying comments in a Post detail screen.
 *
 * This sealed class defines how the UI should react depending on comment retrieval results:
 * - [Loading]: Indicates that comments are being fetched.
 * - [Success]: Contains the list of retrieved comments.
 * - [Error]: Represents possible error conditions, such as empty results or generic failures.
 *
 * Used by [DetailPostViewModel] to manage and expose comment-related UI updates.
 */
sealed class DetailCommentUiState {
    object Loading : DetailCommentUiState()
    data class Success(val comments: List<Comment>) : DetailCommentUiState()
    sealed class Error(open val message: String) : DetailCommentUiState() {
        data class Empty(override val message: String = "No posts found") : Error(message)
        data class Generic(override val message: String = "Unknown error") : Error(message)
    }
}