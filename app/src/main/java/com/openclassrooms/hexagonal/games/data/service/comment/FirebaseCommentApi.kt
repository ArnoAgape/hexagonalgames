package com.openclassrooms.hexagonal.games.data.service.comment

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.utils.NetworkUtils
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.IOException
import kotlin.collections.orEmpty

class FirebaseCommentApi @Inject constructor(private val networkUtils: NetworkUtils) : CommentApi {
    private val db = FirebaseFirestore.getInstance()

    /**
     * Adds a new Comment to the data source using the injected CommentApi.
     *
     * @param comment The Comment object to be added.
     */

    override suspend fun addComment(postId: String, comment: Comment) {
        if (!networkUtils.isNetworkAvailable()) {
            throw IOException("No internet connection")
        }
        val commentWithPostId = comment.copy(postId = postId)
        db.collection("posts")
            .document(postId)
            .collection("comments")
            .add(commentWithPostId)
            .await()
    }

    override fun observeComments(postId: String): Flow<List<Comment>> = callbackFlow {
        val listener = db.collection("posts")
            .document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
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