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

class FirebasePostApi @Inject constructor(private val networkUtils: NetworkUtils) : PostApi {

    private val firestore = FirebaseFirestore.getInstance()
    private val postsCollection = firestore.collection("posts")

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
            postsCollection
                .document(updatedPost.id)
                .set(updatedPost)
                .await()
        } catch (e: Exception) {
            Log.e("FirebasePostApi", "Error while adding post", e)
            throw e
        }
    }

    override fun getCurrentPost(): Flow<Post> = callbackFlow {
        val listener = postsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                val latestPost = snapshot?.toObjects(Post::class.java)?.firstOrNull()
                if (latestPost != null) trySend(latestPost)
            }

        awaitClose { listener.remove() }
    }

    override fun getPostById(postId: String): Flow<Post?> = callbackFlow {
        val listener = postsCollection
            .document(postId)
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

    override suspend fun uploadImageToFirebase(uri: Uri): String? {
        return withContext(Dispatchers.IO + SupervisorJob()) {
            try {
                val fileRef = FirebaseStorage.getInstance()
                    .reference
                    .child("images/${System.currentTimeMillis()}.jpg")

                fileRef.putFile(uri).await()
                val downloadUrl = fileRef.downloadUrl.await()
                downloadUrl.toString()
            } catch (e: Exception) {
                Log.e("FirebaseUpload", "Error while uploading the picture", e)
                null
            }
        }
    }

}
