package com.openclassrooms.hexagonal.games.data.service

import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.core.net.toUri
import kotlinx.coroutines.flow.map

/**
 * This class implements the PostApi interface and provides a fake in-memory data source for Posts.
 * It's intended for testing purposes and simulates a real API.
 */
class PostFakeApi : PostApi {
  private val users = mutableListOf(
    User("1", "Gerry Ariella", "gariella@mail.fr", null),
    User("2", "Brenton Capri", "bcapri@mail.fr", null),
    User("3", "Wally Claud", "wclaud@mail.fr", null)
  )
  
  private val posts = MutableStateFlow(
    listOf(
      Post(
        "5",
        "The Secret of the Flowers",
        "Improve your goldfish's physical fitness by getting him a bicycle.",
        null,
        1629858873, // 25/08/2021
        users[0]
      ),
      Post(
        "4",
        "The Door's Game",
        null,
          "https://picsum.photos/id/85/1080/".toUri(),
        1451638679, // 01/01/2016
        users[2]
      ),
      Post(
        "1",
        "Laughing History",
        "He learned the important lesson that a picnic at the beach on a windy day is a bad idea.",
        null,
        1361696994, // 24/02/2013
        users[0]
      ),
      Post(
        "3",
        "Woman of Years",
        "After fighting off the alligator, Brian still had to face the anaconda.",
        null,
        1346601558, // 02/09/2012
        users[0]
      ),
      Post(
        "2",
        "The Invisible Window",
        null,
        "https://picsum.photos/id/40/1080/".toUri(),
        1210645031, // 13/05/2008
        users[1]
      ),
    )
  )
  
  override fun getPostsOrderByCreationDateDesc(): Flow<List<Post>> =
    posts

  override fun addPost(post: Post) {
    posts.value = listOf(post) + posts.value
  }

  override fun getCurrentPost(): Flow<Post> =
    posts.map { it.maxByOrNull { post -> post.timestamp } ?: it.first() }

  override fun getPostById(postId: String): Flow<Post?> =
    posts.map { it.find { post -> post.id == postId } }

}
