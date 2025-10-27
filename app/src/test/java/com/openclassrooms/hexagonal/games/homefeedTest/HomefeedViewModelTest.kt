package com.openclassrooms.hexagonal.games.homefeedTest

import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.screen.homefeed.HomefeedViewModel
import com.openclassrooms.hexagonal.games.screen.homefeed.PostUiState
import com.openclassrooms.hexagonal.games.ui.common.Event
import com.openclassrooms.hexagonal.games.ui.utils.NetworkUtils
import app.cash.turbine.test
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.domain.model.User
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomefeedViewModelTest {

    private lateinit var viewModel: HomefeedViewModel
    private lateinit var postRepo: PostRepository
    private lateinit var userRepo: UserRepository
    private lateinit var fakeNetwork: NetworkUtils

    @Before
    fun setup() {
        postRepo = mockk()
        userRepo = mockk()
        fakeNetwork = mockk()

        coEvery { userRepo.isUserSignedIn() } returns flowOf(true)
        coEvery { fakeNetwork.isNetworkAvailable() } returns true
        every { postRepo.posts } returns flowOf(emptyList())

        viewModel = HomefeedViewModel(postRepo, userRepo, fakeNetwork)
    }

    @Test
    fun `uiState is Success when repository returns posts`() = runTest {
        val fakePosts = listOf(
            Post(
                "5",
                "The Secret of the Flowers",
                "Improve your goldfish's physical fitness by getting him a bicycle.",
                null,
                1629858873, // 25/08/2021
                User("1", "Gerry Ariella", "gariella@mail.com")
            )
        )
        every { postRepo.posts } returns flowOf(fakePosts)

        viewModel = HomefeedViewModel(postRepo, userRepo, fakeNetwork)

        viewModel.uiState.test {
            val latest = expectMostRecentItem()
            assertTrue("Expected Success but was $latest", latest is PostUiState.Success)
            assertEquals(fakePosts, (latest as PostUiState.Success).posts)
        }
    }

    @Test
    fun `uiState is Error_Empty when repository returns empty list`() = runTest {
        postRepo = mockkClass(PostRepository::class)
        every { postRepo.posts } returns flowOf(emptyList())

        viewModel = HomefeedViewModel(postRepo, userRepo, fakeNetwork)

        viewModel.uiState.test {
            val latest = expectMostRecentItem()
            assertTrue("Expected Error.Empty but was $latest", latest is PostUiState.Error.Empty)
        }
    }

    @Test
    fun `uiState becomes Error_Generic when repository flow throws exception`() = runTest {
        // Arrange
        val errorFlow = flow<List<Post>> { throw Exception("Database failed") }

        postRepo = mockk(relaxed = true)
        every { postRepo.posts } returns errorFlow
        every { userRepo.isUserSignedIn() } returns flowOf(true)
        every { fakeNetwork.isNetworkAvailable() } returns true

        // Act
        viewModel = HomefeedViewModel(postRepo, userRepo, fakeNetwork)

        // Assert
        viewModel.uiState.test {
            val latest = expectMostRecentItem()
            assertTrue("Expected Error.Generic but was $latest", latest is PostUiState.Error.Generic)
            assertEquals("Database failed", (latest as PostUiState.Error.Generic).message)
        }
    }

    @Test
    fun `refreshPosts sends ShowToast event when no network`() = runTest {
        every { fakeNetwork.isNetworkAvailable() } returns false

        viewModel = HomefeedViewModel(postRepo, userRepo, fakeNetwork)

        viewModel.eventsFlow.test {
            viewModel.refreshPosts()
            val event = awaitItem()
            assertTrue(event is Event.ShowToast)
            assertEquals(R.string.no_network, (event as Event.ShowToast).message)
        }
    }
}