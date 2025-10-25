package com.openclassrooms.hexagonal.games.addCommentTest

import app.cash.turbine.test
import com.openclassrooms.hexagonal.games.MainDispatcherRule
import com.openclassrooms.hexagonal.games.data.repository.CommentRepository
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import com.openclassrooms.hexagonal.games.data.service.user.UserFakeApi.users
import com.openclassrooms.hexagonal.games.screen.addComment.AddCommentUiState
import com.openclassrooms.hexagonal.games.screen.addComment.AddCommentViewModel
import com.openclassrooms.hexagonal.games.screen.addPost.FormEvent
import com.openclassrooms.hexagonal.games.ui.common.Event
import com.openclassrooms.hexagonal.games.ui.utils.NetworkUtils
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class AddCommentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var commentRepo: CommentRepository
    private lateinit var userRepo: UserRepository

    private lateinit var fakeNetwork: NetworkUtils
    private lateinit var viewModel: AddCommentViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        commentRepo = mockk()
        userRepo = mockk()
        fakeNetwork = mockk()

        coEvery { userRepo.getCurrentUser() } returns users[0]

        viewModel = AddCommentViewModel(commentRepo, userRepo, fakeNetwork)
    }


    @Test
    fun `isCommentValid emits false when comment is blank`() = runTest {

        viewModel.onAction(FormEvent.CommentChanged(""))
        viewModel.isCommentValid.test {
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `isCommentValid emits true when comment is not blank`() = runTest {
        viewModel.onAction(FormEvent.CommentChanged("test"))

        viewModel.isCommentValid.test {
            skipItems(1) // initialValue = false
            assertTrue(awaitItem())
        }
    }

    @Test
    fun `init loads current user from repository`() = runTest {
        viewModel.user.test {
            skipItems(1) // initialValue = null
            assertEquals("Gerry", viewModel.user.value?.displayName)
        }
    }

    @Test
    fun `onSaveClicked shows error toast when comment is empty`() = runTest {
        viewModel.eventsFlow.test {

            viewModel.onAction(FormEvent.CommentChanged(""))
            viewModel.onSaveClicked("0")

            val event = awaitItem()
            assertTrue(event is Event.ShowToast)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `addComment emits Success when repository succeeds`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        coEvery { commentRepo.addComment(any(), any()) } just Runs
        coEvery { fakeNetwork.isNetworkAvailable() } returns true

        viewModel.onAction(FormEvent.CommentChanged("I'm lovin' it!"))

        // Act
        viewModel.addComment("1")

        advanceUntilIdle()

        // Assert
        val uiState = viewModel.uiState.value
        assertTrue(uiState is AddCommentUiState.Success)
        assertEquals("I'm lovin' it!", (uiState as AddCommentUiState.Success).comment.content)

        coVerify { commentRepo.addComment("1", any()) }
    }

    @Test
    fun `addComment shows no network error when offline`() = runTest {
        coEvery { fakeNetwork.isNetworkAvailable() } returns false
        viewModel.eventsFlow.test {

            viewModel.addComment("post123")

            val event = awaitItem()
            assertTrue(event is Event.ShowToast)
        }

        coVerify(exactly = 0) { commentRepo.addComment(any(), any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `addComment emits Generic error when repository throws`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        coEvery { fakeNetwork.isNetworkAvailable() } returns true
        coEvery { commentRepo.addComment(any(), any()) } throws Exception("DB error")

        viewModel.onAction(FormEvent.CommentChanged("Wow!"))

        // Act
        viewModel.addComment("post123")
        advanceUntilIdle() // exécute tout ce qui est dans viewModelScope.launch

        // Assert
        val state = viewModel.uiState.value
        println(">>> uiState = $state")
        assertTrue(state is AddCommentUiState.Error.Generic)
        coVerify { commentRepo.addComment("post123", any()) }
    }


}