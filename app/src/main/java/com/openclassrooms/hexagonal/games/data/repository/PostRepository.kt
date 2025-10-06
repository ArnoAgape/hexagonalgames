package com.openclassrooms.hexagonal.games.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.openclassrooms.hexagonal.games.data.service.PostApi
import com.openclassrooms.hexagonal.games.domain.model.Post
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
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
        postApi.addPost(post)
        if (post.photoUrl != null) {
            val imageUrl = uploadImageToFirebase(post.photoUrl)
            Log.d("FirebaseUpload", "✅ Image uploadée : $imageUrl")
        }
    }

    suspend fun uploadImageToFirebase(uri: Uri): String? {
        return try {
            val fileRef = FirebaseStorage.getInstance()
                .reference
                .child("images/${System.currentTimeMillis()}.jpg")

            Log.d("FirebaseUpload", "Début de l’upload")
            fileRef.putFile(uri).await()
            Log.d("FirebaseUpload", "En cours d’upload")
            val downloadUrl = fileRef.downloadUrl.await()
            Log.d("FirebaseUpload", "Upload réussi : $downloadUrl")
            downloadUrl.toString()
        } catch (e: Exception) {
            Log.e("FirebaseUpload", "Erreur lors de l’upload", e)
            e.printStackTrace()
            null
        }
    }

}
