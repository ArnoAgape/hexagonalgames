package com.openclassrooms.hexagonal.games.data.service.comment

import com.openclassrooms.hexagonal.games.domain.model.Comment
import kotlinx.coroutines.flow.Flow

interface CommentApi {
    suspend fun addComment(postId: String, comment: Comment)
    fun observeComments(postId: String): Flow<List<Comment>>
}