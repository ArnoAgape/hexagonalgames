package com.openclassrooms.hexagonal.games.screen.detailPost

import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.domain.model.Post

data class DetailCommentUiState(
    val isLoading: Boolean = false,
    val comments: List<Comment> = emptyList(),
    val error: String? = null
)