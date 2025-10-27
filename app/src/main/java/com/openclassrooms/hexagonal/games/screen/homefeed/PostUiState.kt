package com.openclassrooms.hexagonal.games.screen.homefeed

import com.openclassrooms.hexagonal.games.domain.model.Post

/**
 * Represents the possible UI states for displaying a list of Posts.
 *
 * This sealed class allows the UI layer to react to data loading and errors:
 * - [Loading]: Shown while posts are being retrieved.
 * - [Success]: Contains the list of successfully loaded posts.
 * - [Error]: Represents issues such as an empty feed or generic failures.
 *
 * Commonly used in the Home Feed or Post List screens via a corresponding ViewModel.
 */
sealed class PostUiState {
    object Loading : PostUiState()
    data class Success(val posts: List<Post>) : PostUiState()
    sealed class Error : PostUiState() {
        data class Empty(val message: String = "No posts found") : Error()
        data class Generic(val message: String = "Unknown error") : Error()
    }
}