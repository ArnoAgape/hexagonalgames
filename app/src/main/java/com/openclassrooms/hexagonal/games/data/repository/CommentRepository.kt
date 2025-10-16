package com.openclassrooms.hexagonal.games.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.openclassrooms.hexagonal.games.domain.model.Comment
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class provides a repository for accessing and managing Comment data.
 * It utilizes dependency injection to retrieve a CommentApi instance for interacting
 * with the data source. The class is marked as a Singleton using @Singleton annotation,
 * ensuring there's only one instance throughout the application.
 */
@Singleton
class CommentRepository @Inject constructor() {

    private val db = FirebaseFirestore.getInstance()

    /**
     * Adds a new Comment to the data source using the injected CommentApi.
     *
     * @param comment The Comment object to be added.
     */

    suspend fun addComment(postId: String, comment: Comment) {
        db.collection("posts")
            .document(postId)
            .collection("comments")
            .add(comment)
            .await()
    }

    fun observeComments(postId: String): Flow<List<Comment>> = callbackFlow {
        val listener = db.collection("posts")
            .document(postId)
            .collection("comments")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val comments = snapshot?.toObjects(Comment::class.java).orEmpty()
                trySend(comments)
            }

        awaitClose { listener.remove() }
    }

}