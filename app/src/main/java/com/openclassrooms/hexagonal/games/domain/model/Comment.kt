package com.openclassrooms.hexagonal.games.domain.model

import java.io.Serializable

data class Comment (
    /**
     * Unique identifier for the Comment.
     */
    val id: String,

    /**
     * Content of the Comment.
     */
    val content: String,

    /**
     * Timestamp representing the creation date and time of the Post in milliseconds since epoch.
     */
    val timestamp: Long,

    /**
     * User object representing the author of the Post.
     */
    val author: User?
) : Serializable
