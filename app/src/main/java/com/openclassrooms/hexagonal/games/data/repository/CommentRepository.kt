package com.openclassrooms.hexagonal.games.data.repository

import com.openclassrooms.hexagonal.games.data.service.comment.CommentApi
import com.openclassrooms.hexagonal.games.domain.model.Comment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class responsible for accessing and managing [Comment] data.
 *
 * This repository acts as an intermediary between the ViewModel layer and
 * the underlying [CommentApi] data source, delegating all operations to it.
 *
 * It is annotated with [@Singleton] to ensure only one instance exists
 * throughout the application lifecycle, and leverages dependency injection
 * to obtain the [CommentApi] implementation.
 *
 * @constructor Injects a [CommentApi] instance for data source interaction.
 */
@Singleton
class CommentRepository @Inject constructor(
    private val commentApi: CommentApi
) {
    /**
     * Adds a new [Comment] to a specific Post.
     *
     * @param postId The unique identifier of the Post to which the comment belongs.
     * @param comment The [Comment] object to be added.
     */
    suspend fun addComment(postId: String, comment: Comment) =
        commentApi.addComment(postId, comment)

    /**
     * Observes the list of [Comment]s associated with a given Post.
     *
     * @param postId The unique identifier of the Post.
     * @return A [Flow] emitting lists of [Comment]s for the specified Post.
     */
    fun observeComments(postId: String): Flow<List<Comment>> =
        commentApi.observeComments(postId)
}