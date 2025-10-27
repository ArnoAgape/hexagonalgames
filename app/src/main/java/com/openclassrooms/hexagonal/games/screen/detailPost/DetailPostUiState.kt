package com.openclassrooms.hexagonal.games.screen.detailPost

import com.openclassrooms.hexagonal.games.domain.model.Post

/**
 * Represents the different UI states for displaying detailed information about a Post.
 *
 * This sealed class is used to manage and communicate the loading, success, and
 * error states when retrieving post details:
 * - [Loading]: Indicates data is currently being fetched.
 * - [Success]: Contains the successfully retrieved [Post].
 * - [Error]: Represents failure scenarios, such as missing or unavailable posts.
 *
 * Typically observed from [DetailPostViewModel] to update the corresponding UI.
 */
sealed class DetailPostUiState {
    object Loading : DetailPostUiState()
    data class Success(val post: Post) : DetailPostUiState()
    sealed class Error : DetailPostUiState() {
        data class Empty(val message: String = "No posts found") : Error()
        data class Generic(val message: String = "Unknown error") : Error()
    }
}