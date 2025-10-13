package com.openclassrooms.hexagonal.games.data.repository

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.storage.FirebaseStorage
import com.openclassrooms.hexagonal.games.data.service.PostApi
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
        // Upload de l'image
        val imageUrl = post.photoUrl?.let { uri ->
            uploadImageToFirebase(uri)?.toUri()
        }

        // Construction du post complet
        val postToSave = post.copy(photoUrl = imageUrl)

        // Délégation à l’API
        postApi.addPost(postToSave)
        Log.d("PostRepository", "✅ Post envoyé vers Firestore avec imageUrl=$imageUrl")
    }


    suspend fun uploadImageToFirebase(uri: Uri): String? {
        return withContext(Dispatchers.IO + SupervisorJob()) {
            try {
                Log.d("FirebaseUpload", "🚀 Début upload : $uri")

                val fileRef = FirebaseStorage.getInstance()
                    .reference
                    .child("images/${System.currentTimeMillis()}.jpg")

                Log.d("FirebaseUpload", "🟡 putFile start...")
                fileRef.putFile(uri).await()
                Log.d("FirebaseUpload", "✅ putFile terminé")

                val downloadUrl = fileRef.downloadUrl.await()
                Log.d("FirebaseUpload", "✅ Download URL : $downloadUrl")

                downloadUrl.toString()
            } catch (e: Exception) {
                Log.e("FirebaseUpload", "❌ Erreur upload Firebase", e)
                null
            }
        }
    }

    fun getPostById(postId: String): Flow<Post?> = postApi.getPostById(postId)

}
