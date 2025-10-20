package com.openclassrooms.hexagonal.games.data.service.post

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.utils.NetworkUtils
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
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
            postsCollection
                .document(post.id)
                .set(post)
                .await()
            Log.d("FirebasePostApi", "Post added successfully: ${post.title}")

        } catch (e: Exception) {
            Log.e("FirebasePostApi", "Error while adding the post", e)
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

}
