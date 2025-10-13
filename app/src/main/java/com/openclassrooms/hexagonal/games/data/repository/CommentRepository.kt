package com.openclassrooms.hexagonal.games.data.repository

import android.util.Log
import com.openclassrooms.hexagonal.games.data.service.CommentApi
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

    /**
     * Retrieves a Flow object containing a list of Comments ordered by creation date
     * in ascending order.
     *
     * @return Flow containing a list of Comments.
     */
    val comments: Flow<List<Comment>> = commentApi.getCommentsOrderByCreationDateAsc()

    /**
     * Adds a new Comment to the data source using the injected CommentApi.
     *
     * @param comment The Comment object to be added.
     */

    fun addComment(comment: Comment) {

        // Délégation à l’API
        commentApi.addComment(comment)
        Log.d("CommentRepository", "✅ Commentaire envoyé vers Firestore avec comment=$comment")
    }

    fun getCommentById(commentId: String): Flow<Comment?> = commentApi.getCommentById(commentId)

}