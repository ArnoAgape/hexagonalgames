package com.openclassrooms.hexagonal.games.data.service.comment

import com.openclassrooms.hexagonal.games.domain.model.Comment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake API simulant la logique Firebase des commentaires.
 */
class CommentFakeApi : CommentApi {

    // Map<PostId, MutableList<Comment>>
    private val commentsMap = mutableMapOf<String, MutableStateFlow<MutableList<Comment>>>()

    override suspend fun addComment(postId: String, comment: Comment) {
        val comments = commentsMap.getOrPut(postId) { MutableStateFlow(mutableListOf()) }
        val current = comments.value.toMutableList()
        current.add(0, comment)
        comments.value = current
    }

    override fun observeComments(postId: String): Flow<List<Comment>> {
        val comments = commentsMap.getOrPut(postId) { MutableStateFlow(mutableListOf()) }
        return comments.map { it.sortedByDescending { c -> c.timestamp } }
    }
}
