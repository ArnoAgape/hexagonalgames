package com.openclassrooms.hexagonal.games.data.service.post

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.ui.utils.NetworkUtils
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Firebase-based implementation of the [PostApi] interface.
 *
 * This class provides access to Post data stored in Firebase Firestore and
 * supports image uploads via Firebase Storage. It also integrates with
 * [NetworkUtils] to ensure that write operations only occur when the
 * device is connected to the internet.
 *
 * Posts are stored in a top-level Firestore collection named `"posts"`.
 *
 * @constructor Injects a [NetworkUtils] instance for connectivity validation.
 */
class FirebasePostApi @Inject constructor(
    private val networkUtils: NetworkUtils
) : PostApi {

    private val firestore = FirebaseFirestore.getInstance()
    private val postsCollection = firestore.collection("posts")

    /**
     * Observes all [Post]s ordered by their `timestamp` field in descending order.
     * Emits updates in real time whenever the collection changes.
     *
     * @return A [Flow] emitting lists of [Post]s sorted by newest first.
     */
    override fun getPostsOrderByCreationDateDesc(): Flow<List<Post>> = callbackFlow {
        val listener = postsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val posts = snapshot?.toObjects(Post::class.java).orEmpty()
                trySend(posts)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Adds or updates a [Post] document in Firestore.
     *
     * If the post contains an image with a `content://` URI, the image is uploaded
     * to Firebase Storage and the post is updated with the resulting download URL.
     *
     * @param post The [Post] object to add or update.
     * @throws IOException If there is no network connection.
     */
    override suspend fun addPost(post: Post) {
        if (!networkUtils.isNetworkAvailable()) {
            throw IOException("No internet connection")
        }
        try {
            var updatedPost = post
            post.photoUrl?.let { uriString ->
                val uri = uriString.toUri()
                if (uri.scheme == "content") {
                    val downloadUrl = uploadImageToFirebase(uri)
                    if (downloadUrl != null) {
                        updatedPost = post.copy(photoUrl = downloadUrl)
                    }
                }
            }
            postsCollection.document(updatedPost.id).set(updatedPost).await()
        } catch (e: Exception) {
            Log.e("FirebasePostApi", "Error while adding post", e)
            throw e
        }
    }

    /**
     * Observes the most recently created [Post].
     *
     * @return A [Flow] emitting the latest [Post] whenever it changes.
     */
    override fun getCurrentPost(): Flow<Post> = callbackFlow {
        val listener = postsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                snapshot?.toObjects(Post::class.java)?.firstOrNull()?.let { trySend(it) }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Observes a specific [Post] by its unique identifier.
     *
     * @param postId The ID of the post to observe.
     * @return A [Flow] emitting the matching [Post] or `null` if not found.
     */
    override fun getPostById(postId: String): Flow<Post?> = callbackFlow {
        val listener = postsCollection.document(postId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val post = snapshot?.toObject(Post::class.java)
                trySend(post)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Uploads an image to Firebase Storage and returns its download URL.
     *
     * @param uri The [Uri] of the image to upload.
     * @return The download URL as a [String], or `null` if the upload fails.
     */
    override suspend fun uploadImageToFirebase(uri: Uri): String? {
        return withContext(Dispatchers.IO + SupervisorJob()) {
            try {
                val fileRef = FirebaseStorage.getInstance()
                    .reference
                    .child("images/${System.currentTimeMillis()}.jpg")

                fileRef.putFile(uri).await()
                fileRef.downloadUrl.await().toString()
            } catch (e: Exception) {
                Log.e("FirebaseUpload", "Error while uploading the picture", e)
                null
            }
        }
    }
}