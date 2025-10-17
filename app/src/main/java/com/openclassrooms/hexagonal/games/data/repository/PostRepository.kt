package com.openclassrooms.hexagonal.games.data.repository

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.storage.FirebaseStorage
import com.openclassrooms.hexagonal.games.data.service.post.PostApi
import com.openclassrooms.hexagonal.games.domain.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class provides a repository for accessing and managing Post data.
 * It utilizes dependency injection to retrieve a PostApi instance for interacting
 * with the data source. The class is marked as a Singleton using @Singleton annotation,
 * ensuring there's only one instance throughout the application.
 */
@Singleton
class PostRepository @Inject constructor(private val postApi: PostApi) {

    /**
     * Retrieves a Flow object containing a list of Posts ordered by creation date
     * in descending order.
     *
     * @return Flow containing a list of Posts.
     */
    val posts: Flow<List<Post>> = postApi.getPostsOrderByCreationDateDesc()

    /**
     * Adds a new Post to the data source using the injected PostApi.
     *
     * @param post The Post object to be added.
     */

    suspend fun addPost(post: Post) {
        // Uploading image
        val imageUrl = post.photoUrl?.let { uriString ->
            val uri = uriString.toUri()
            uploadImageToFirebase(uri)
        }

        // Creating post
        val postToSave = post.copy(photoUrl = imageUrl)

        // API
        postApi.addPost(postToSave)
    }

    suspend fun uploadImageToFirebase(uri: Uri): String? {
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
    fun getPostById(postId: String): Flow<Post?> = postApi.getPostById(postId)
}
