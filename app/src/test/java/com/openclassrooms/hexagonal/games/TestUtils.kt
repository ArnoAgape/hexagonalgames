package com.openclassrooms.hexagonal.games

import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User

object TestUtils {

    fun fakePost(id: String): Post {
        return Post(
            id,
            "The Secret of the Flowers",
            "Improve your goldfish's physical fitness by getting him a bicycle.",
            null,
            1629858873, // 25/08/2021
            User("1", "Gerry Ariella", "gariella@mail.com")
        )
    }

    fun fakeComment(id: String): Comment {
        return Comment(
            id,
            "",
            "",
            1629858873,
            User("1", "Gerry Ariella", "gariella@mail.com")
        )
    }

    fun fakeUser(id: String): User {
        return User(id, "Gerry Ariella", "gariella@mail.com")
    }

}