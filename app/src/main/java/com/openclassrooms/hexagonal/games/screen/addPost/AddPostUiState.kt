package com.openclassrooms.hexagonal.games.screen.addPost

import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import java.util.UUID

data class AddPostUiState(
    val post: Post,
    val user: User?,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val error: FormError? = null
) {
    companion object {
        fun initial() = AddPostUiState(
            post = Post(
                id = UUID.randomUUID().toString(),
                title = "",
                description = "",
                photoUrl = null,
                timestamp = System.currentTimeMillis(),
                author = null
            ),
            user = null
        )
    }
}