package com.openclassrooms.hexagonal.games.data.service.post

import android.net.Uri
import com.openclassrooms.hexagonal.games.domain.model.Post
import kotlinx.coroutines.flow.Flow

/**
 * Defines the contract for interacting with Post data from a data source.
 * It provides methods for retrieving, adding, and observing Posts,
 * abstracting away the underlying data persistence and networking layers.
 */
interface PostApi {

  /**
   * Retrieves a [Flow] emitting lists of [Post]s ordered by creation date in descending order.
   *
   * @return A [Flow] emitting lists of [Post]s sorted by newest first.
   */
  fun getPostsOrderByCreationDateDesc(): Flow<List<Post>>

  /**
   * Adds a new [Post] to the data source.
   *
   * @param post The [Post] object to add.
   */
  suspend fun addPost(post: Post)

  /**
   * Observes the currently selected or active [Post].
   *
   * @return A [Flow] emitting the current [Post].
   */
  fun getCurrentPost(): Flow<Post>

  /**
   * Retrieves a [Flow] emitting a [Post] matching the given [postId].
   *
   * @param postId The unique identifier of the post to retrieve.
   * @return A [Flow] emitting the matching [Post] or `null` if not found.
   */
  fun getPostById(postId: String): Flow<Post?>

  /**
   * Uploads an image to Firebase Storage and returns its download URL.
   *
   * @param uri The [Uri] of the image to upload.
   * @return The download URL as a [String], or `null` if the upload fails.
   */
  suspend fun uploadImageToFirebase(uri: Uri): String?
}