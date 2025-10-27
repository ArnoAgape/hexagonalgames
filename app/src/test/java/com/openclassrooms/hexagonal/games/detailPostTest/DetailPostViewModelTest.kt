package com.openclassrooms.hexagonal.games.detailPostTest

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.openclassrooms.hexagonal.games.MainDispatcherRule
import com.openclassrooms.hexagonal.games.TestUtils
import com.openclassrooms.hexagonal.games.data.repository.CommentRepository
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import com.openclassrooms.hexagonal.games.screen.detailPost.DetailCommentUiState
import com.openclassrooms.hexagonal.games.screen.detailPost.DetailPostUiState
import com.openclassrooms.hexagonal.games.screen.detailPost.DetailPostViewModel
import com.openclassrooms.hexagonal.games.ui.common.Event
import com.openclassrooms.hexagonal.games.ui.utils.NetworkUtils
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


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
    fun `uiState emits Success when post and comments are loaded`() = runTest {
        // Arrange
        val fakePost = TestUtils.fakePost(id = "0")
        val fakeComments = listOf(TestUtils.fakeComment("1"))

        coEvery { postRepo.getPostById(fakePost.id) } returns flowOf(fakePost)
        coEvery { commentRepo.observeComments(fakePost.id) } returns flowOf(fakeComments)
        coEvery { userRepo.isUserSignedIn() } returns flowOf(true)
        every { fakeNetwork.isNetworkAvailable() } returns true

        val savedState = SavedStateHandle(mapOf("postId" to fakePost.id))
        val viewModel = DetailPostViewModel(postRepo, commentRepo, userRepo, savedState, fakeNetwork)

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.postState is DetailPostUiState.Success)
        assertTrue(state.commentState is DetailCommentUiState.Success)
    }

    @Test
    fun `uiState emits Error_Generic when repository throws exception`() = runTest {
        // Arrange
        val fakePost = TestUtils.fakePost("1")

        coEvery { postRepo.getPostById(fakePost.id) } returns flow { throw Exception("DB error") }
        coEvery { commentRepo.observeComments(fakePost.id) } returns flow { throw Exception("Network error") }
        coEvery { userRepo.isUserSignedIn() } returns flowOf(false)
        every { fakeNetwork.isNetworkAvailable() } returns true

        val savedState = SavedStateHandle(mapOf("postId" to fakePost.id))
        val viewModel = DetailPostViewModel(postRepo, commentRepo, userRepo, savedState, fakeNetwork)

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.postState is DetailPostUiState.Error.Generic)
        assertTrue(state.commentState is DetailCommentUiState.Error.Generic)
    }

    @Test
    fun `uiState emits Error_Empty when comments list is empty`() = runTest {
        // Arrange
        val fakePost = TestUtils.fakePost("1")

        coEvery { postRepo.getPostById(fakePost.id) } returns flowOf(fakePost)
        coEvery { commentRepo.observeComments(fakePost.id) } returns flowOf(emptyList())
        coEvery { userRepo.isUserSignedIn() } returns flowOf(true)
        every { fakeNetwork.isNetworkAvailable() } returns true

        val savedState = SavedStateHandle(mapOf("postId" to fakePost.id))
        val viewModel = DetailPostViewModel(postRepo, commentRepo, userRepo, savedState, fakeNetwork)

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.postState is DetailPostUiState.Success)
        assertTrue(state.commentState is DetailCommentUiState.Error.Empty)
    }

    @Test
    fun `refreshData sends ShowToast when no network`() = runTest {
        // Arrange
        val fakePost = TestUtils.fakePost("1")

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