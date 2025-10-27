package com.openclassrooms.hexagonal.games.data.service.comment

import com.openclassrooms.hexagonal.games.domain.model.Comment
import kotlinx.coroutines.flow.Flow

/**
 * Defines the contract for interacting with comments related to Posts.
 * Provides methods for adding and observing comments, abstracting the
 * underlying data source (e.g., Firestore, local database).
 */
interface CommentApi {

    /**
     * Adds a new [Comment] to a given Post.
     *
     * @param postId The unique identifier of the Post to which the comment belongs.
     * @param comment The [Comment] object to be added.
     */
    suspend fun addComment(postId: String, comment: Comment)

    /**
     * Observes the list of comments associated with a given Post.
     *
     * @param postId The unique identifier of the Post.
     * @return A [Flow] emitting lists of [Comment]s for the specified Post.
     */
    fun observeComments(postId: String): Flow<List<Comment>>
}