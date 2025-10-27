package com.openclassrooms.hexagonal.games.screen.addPost

import com.openclassrooms.hexagonal.games.domain.model.Post

/**
 * Represents the different UI states for the Add Post screen.
 *
 * This sealed class models the lifecycle of adding a post, including:
 * - [Idle]: The default state before any user action.
 * - [Loading]: The state shown while the post is being uploaded or processed.
 * - [Success]: Indicates that the post was successfully created.
 * - [Error]: Represents failure cases such as missing account or unknown errors.
 *
 * Used by [AddPostViewModel] to drive the UI reactively through state changes.
 */
sealed class AddPostUiState {
    object Idle : AddPostUiState()
    object Loading : AddPostUiState()
    data class Success(val post: Post) : AddPostUiState()
    sealed class Error : AddPostUiState() {
        data class NoAccount(val message: String = "No account found") : Error()
        data class Generic(val message: String = "Unknown error") : Error()
    }
}