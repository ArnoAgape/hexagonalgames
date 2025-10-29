package com.openclassrooms.hexagonal.games.data.service.comment

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.ui.utils.NetworkUtils
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.IOException
import kotlin.collections.orEmpty

/**
 * Firebase-based implementation of the [CommentApi] interface.
 *
 * This class handles comment persistence and observation using Firebase Firestore.
 * It also integrates with [NetworkUtils] to ensure operations only proceed when
 * the device has an active network connection.
 *
 * Each Post document in Firestore contains a sub-collection of comments.
 * Comments are stored with ascending order based on their `timestamp` field.
 *
 * @constructor Injects a [NetworkUtils] instance for network connectivity checks.
 */
class FirebaseCommentApi @Inject constructor(
    private val networkUtils: NetworkUtils
) : CommentApi {

    private val db = FirebaseFirestore.getInstance()

    /**
     * Adds a new [Comment] document to the specified Post's subcollection in Firestore.
     * Throws an [IOException] if the device is offline.
     *
     * @param postId The unique identifier of the Post to which the comment belongs.
     * @param comment The [Comment] object to be added.
     * @throws IOException If no internet connection is available.
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

    /**
     * Observes the list of comments for a given Post in real time.
     *
     * Uses a Firestore snapshot listener to emit updates whenever comments
     * are added, modified, or removed.
     *
     * @param postId The unique identifier of the Post.
     * @return A [Flow] emitting ordered lists of [Comment]s.
     */
    override fun observeComments(postId: String): Flow<List<Comment>> {
        return db.collection("posts")
            .document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .dataObjects()
    }
}