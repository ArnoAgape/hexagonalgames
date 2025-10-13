package com.openclassrooms.hexagonal.games.data.service

import com.openclassrooms.hexagonal.games.domain.model.Comment
import kotlinx.coroutines.flow.Flow

/**
 * This interface defines the contract for interacting with Comment data from a data source.
 * It outlines the methods for retrieving and adding Comments, abstracting the underlying
 * implementation details of fetching and persisting data.
 */
interface CommentApi {
  /**
   * Retrieves a list of Comments ordered by their creation date in ascending order.
   *
   * @return A list of Comments sorted by creation date (oldest first).
   */
  fun getCommentsOrderByCreationDateAsc(): Flow<List<Comment>>
  
  /**
   * Adds a new Comment to the data source.
   *
   * @param comment The Comment object to be added.
   */
  fun addComment(comment: Comment)

  fun getCurrentComment() : Flow<Comment>

  fun getCommentById(commentId: String): Flow<Comment?>
}
