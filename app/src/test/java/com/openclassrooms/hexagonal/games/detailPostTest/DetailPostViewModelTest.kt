package com.openclassrooms.hexagonal.games.detailPostTest

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.openclassrooms.hexagonal.games.MainDispatcherRule
import com.openclassrooms.hexagonal.games.data.repository.CommentRepository
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import com.openclassrooms.hexagonal.games.screen.detailPost.DetailCommentUiState
import com.openclassrooms.hexagonal.games.screen.detailPost.DetailPostUiState
import com.openclassrooms.hexagonal.games.screen.detailPost.DetailPostViewModel
import com.openclassrooms.hexagonal.games.ui.common.Event
import com.openclassrooms.hexagonal.games.ui.utils.NetworkUtils
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailPostViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var postRepo: PostRepository
    private lateinit var userRepo: UserRepository

    private lateinit var commentRepo: CommentRepository

    private lateinit var fakeNetwork: NetworkUtils

    @Before
    fun setup() {
        postRepo = mockk()
        userRepo = mockk()
        commentRepo = mockk()
        fakeNetwork = mockk()
    }

    @Test
    fun `uiState emits Success when post and comments are loaded`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        val fakePost = Post(
            "5",
            "The Secret of the Flowers",
            "Improve your goldfish's physical fitness by getting him a bicycle.",
            null,
            1629858873, // 25/08/2021
            User("1", "Gerry Ariella", "gariella@mail.com")
        )
        val fakeComments = listOf(
            Comment(
                "1",
                "1",
                "I'm lovin' it!",
                1760880237, // 19/10/2025
                User("1", "Gerry", "gerry@example.com")
            )
        )

        coEvery { postRepo.getPostById(fakePost.id) } returns flowOf(fakePost)
        coEvery { commentRepo.observeComments(fakePost.id) } returns flowOf(fakeComments)
        coEvery { userRepo.isUserSignedIn() } returns flowOf(true)
        every { fakeNetwork.isNetworkAvailable() } returns true

        val savedState = SavedStateHandle(mapOf("postId" to fakePost.id))
        val viewModel = DetailPostViewModel(postRepo, commentRepo, userRepo, savedState, fakeNetwork)

        // Act
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.postState is DetailPostUiState.Success)
        assertTrue(state.commentState is DetailCommentUiState.Success)
    }

    @Test
    fun `uiState emits Error_Generic when repository throws exception`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        val fakePost = Post(
            "5",
            "The Secret of the Flowers",
            "Improve your goldfish's physical fitness by getting him a bicycle.",
            null,
            1629858873, // 25/08/2021
            User("1", "Gerry Ariella", "gariella@mail.com")
        )

        coEvery { postRepo.getPostById(fakePost.id) } returns flow { throw Exception("DB error") }
        coEvery { commentRepo.observeComments(fakePost.id) } returns flow { throw Exception("Network error") }
        coEvery { userRepo.isUserSignedIn() } returns flowOf(false)
        every { fakeNetwork.isNetworkAvailable() } returns true

        val savedState = SavedStateHandle(mapOf("postId" to fakePost.id))
        val viewModel = DetailPostViewModel(postRepo, commentRepo, userRepo, savedState, fakeNetwork)

        // Act
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.postState is DetailPostUiState.Error.Generic)
        assertTrue(state.commentState is DetailCommentUiState.Error.Generic)
    }

    @Test
    fun `uiState emits Error_Empty when comments list is empty`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        val fakePost = Post(
            "5",
            "The Secret of the Flowers",
            "Improve your goldfish's physical fitness by getting him a bicycle.",
            null,
            1629858873, // 25/08/2021
            User("1", "Gerry Ariella", "gariella@mail.com")
        )

        coEvery { postRepo.getPostById(fakePost.id) } returns flowOf(fakePost)
        coEvery { commentRepo.observeComments(fakePost.id) } returns flowOf(emptyList())
        coEvery { userRepo.isUserSignedIn() } returns flowOf(true)
        every { fakeNetwork.isNetworkAvailable() } returns true

        val savedState = SavedStateHandle(mapOf("postId" to fakePost.id))
        val viewModel = DetailPostViewModel(postRepo, commentRepo, userRepo, savedState, fakeNetwork)

        // Act
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.postState is DetailPostUiState.Success)
        assertTrue(state.commentState is DetailCommentUiState.Error.Empty)
    }

    @Test
    fun `refreshData sends ShowToast when no network`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        val fakePost = Post(
            "5",
            "The Secret of the Flowers",
            "Improve your goldfish's physical fitness by getting him a bicycle.",
            null,
            1629858873, // 25/08/2021
            User("1", "Gerry Ariella", "gariella@mail.com")
        )

        coEvery { postRepo.getPostById(fakePost.id) } returns flowOf(fakePost)
        coEvery { commentRepo.observeComments(fakePost.id) } returns flowOf(emptyList())
        coEvery { userRepo.isUserSignedIn() } returns flowOf(true)
        every { fakeNetwork.isNetworkAvailable() } returns false

        val savedState = SavedStateHandle(mapOf("postId" to fakePost.id))
        val viewModel = DetailPostViewModel(postRepo, commentRepo, userRepo, savedState, fakeNetwork)

        // Act + Assert
        viewModel.eventsFlow.test {
            viewModel.refreshData()

            val event = awaitItem()
            assertTrue(event is Event.ShowToast)
            cancelAndIgnoreRemainingEvents()
        }
    }

}