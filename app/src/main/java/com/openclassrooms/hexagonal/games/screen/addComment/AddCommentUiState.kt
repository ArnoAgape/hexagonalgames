package com.openclassrooms.hexagonal.games.screen.addComment

import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.domain.model.User
import com.openclassrooms.hexagonal.games.screen.addPost.FormError
import java.util.UUID

data class AddCommentUiState(
    val comment: Comment,
    val user: User?,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val error: FormError? = null
) {
    companion object {
        fun initial() = AddCommentUiState(
            comment = Comment(
                id = UUID.randomUUID().toString(),
                content = "",
                timestamp = System.currentTimeMillis(),
                author = null
            ),
            user = null
        )
    }
}