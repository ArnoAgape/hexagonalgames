package com.openclassrooms.hexagonal.games.data.repository

import com.openclassrooms.hexagonal.games.data.service.comment.CommentApi
import com.openclassrooms.hexagonal.games.domain.model.Comment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class provides a repository for accessing and managing Comment data.
 * It utilizes dependency injection to retrieve a CommentApi instance for interacting
 * with the data source. The class is marked as a Singleton using @Singleton annotation,
 * ensuring there's only one instance throughout the application.
 */
@Singleton
class CommentRepository @Inject constructor(private val commentApi: CommentApi) {
    suspend fun addComment(postId: String, comment: Comment) = commentApi.addComment(postId, comment)
    fun observeComments(postId: String): Flow<List<Comment>> = commentApi.observeComments(postId)
}
