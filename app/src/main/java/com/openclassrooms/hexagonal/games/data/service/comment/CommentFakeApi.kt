package com.openclassrooms.hexagonal.games.data.service.comment

import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake API simulant la logique Firebase des commentaires.
 */
object CommentFakeApi : CommentApi {

    private val users = mutableListOf(
        User("1", "Gerry", "gerry@example.com"),
        User("2", "Brenton", "brenton@example.com"),
        User("3", "Wally", "wally@example.com")
    )

    val comments = MutableStateFlow(
        mutableListOf(
            Comment(
                "1",
                "1",
                "I'm lovin' it!",
                1760880237, // 19/10/2025
                users[0]
            ),
            Comment(
                "2",
                "2",
                "Nothing is impossible, thanks!",
                1760200000, // 11/10/2025
                users[1]
            ),
            Comment(
                "3",
                "3",
                "Get up, stand up, go for it",
                1760100000, // 10/10/2025
                users[2]
            )
        )
    )

    private val commentsMap = mutableMapOf<String, MutableStateFlow<MutableList<Comment>>>()

    override suspend fun addComment(postId: String, comment: Comment) {
        val comments = commentsMap.getOrPut(postId) { MutableStateFlow(mutableListOf()) }
        val current = comments.value.toMutableList()
        current.add(0, comment)
        comments.value = current
    }

    override fun observeComments(postId: String): Flow<List<Comment>> {
        val comments = commentsMap.getOrPut(postId) { MutableStateFlow(mutableListOf()) }
        return comments.map { it.sortedByDescending { c -> c.timestamp } }
    }
}
