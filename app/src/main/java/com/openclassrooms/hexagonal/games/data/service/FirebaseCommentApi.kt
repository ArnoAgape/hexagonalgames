package com.openclassrooms.hexagonal.games.data.service

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.openclassrooms.hexagonal.games.domain.model.Comment
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseCommentApi : CommentApi {

    private val firestore = FirebaseFirestore.getInstance()
    private val commentsCollection = firestore.collection("comments")

    override fun getCommentsOrderByCreationDateAsc(): Flow<List<Comment>> = callbackFlow {
        val listener = commentsCollection
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

    override fun addComment(comment: Comment) {
        commentsCollection
            .document(comment.id)
            .set(comment)
            .addOnSuccessListener {
                Log.d("FirebaseCommentApi", "Comment added successfully: ${comment.content}")
            }
            .addOnFailureListener {
                Log.e("FirebaseCommentApi", "Error while adding the comment", it)
            }
    }

    override fun getCurrentComment(): Flow<Comment> = callbackFlow {
        val listener = commentsCollection
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                val latestComment = snapshot?.toObjects(Comment::class.java)?.firstOrNull()
                if (latestComment != null) trySend(latestComment)
            }

        awaitClose { listener.remove() }
    }

    override fun getCommentById(commentId: String): Flow<Comment?> = callbackFlow {
        val listener = commentsCollection
            .document(commentId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                val comment = snapshot?.toObject(Comment::class.java)
                trySend(comment)
            }

        awaitClose { listener.remove() }
    }

}
