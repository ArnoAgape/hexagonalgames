package com.openclassrooms.hexagonal.games.addPostTest

import android.net.Uri
import app.cash.turbine.test
import com.openclassrooms.hexagonal.games.MainDispatcherRule
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import com.openclassrooms.hexagonal.games.data.service.user.UserFakeApi.users
import com.openclassrooms.hexagonal.games.screen.addPost.AddPostUiState
import com.openclassrooms.hexagonal.games.screen.addPost.AddPostViewModel
import com.openclassrooms.hexagonal.games.screen.addPost.FormEvent
import com.openclassrooms.hexagonal.games.ui.common.Event
import com.openclassrooms.hexagonal.games.ui.utils.NetworkUtils
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class AddPostViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var postRepo: PostRepository
    private lateinit var userRepo: UserRepository

    private lateinit var fakeNetwork: NetworkUtils
    private lateinit var viewModel: AddPostViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        postRepo = mockk()
        userRepo = mockk()
        fakeNetwork = mockk()

        coEvery { userRepo.getCurrentUser() } returns users[0]

        viewModel = AddPostViewModel(postRepo, userRepo, fakeNetwork)
    }


    @Test
    fun `isPostValid emits false when the title of the post is blank`() = runTest {

        viewModel.onAction(FormEvent.TitleChanged(""))
        viewModel.isPostValid.test {
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `isPostValid emits true when title and photo are not blank`() = runTest {

        val mockUri = mockk<Uri>()

        every { mockUri.toString() } returns "content://media/picker/image123"
        every { mockUri.path } returns "/storage/emulated/0/DCIM/image123.jpg"

        viewModel.onAction(FormEvent.TitleChanged("New game"))
        viewModel.onAction(FormEvent.PhotoChanged(mockUri))

        viewModel.isPostValid.test {
            skipItems(1) // initialValue = false
            assertTrue(awaitItem())
        }
    }

    @Test
    fun `onSaveClicked shows error toast when the title of the post is empty`() = runTest {
        viewModel.eventsFlow.test {

            viewModel.onAction(FormEvent.TitleChanged(""))
            viewModel.onSaveClicked()

            val event = awaitItem()
            assertTrue(event is Event.ShowToast)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `addPost emits Success when repository succeeds`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        coEvery { postRepo.addPost(any()) } just Runs
        coEvery { fakeNetwork.isNetworkAvailable() } returns true

        viewModel.onAction(FormEvent.TitleChanged("New game"))
        viewModel.onAction(FormEvent.DescriptionChanged("The game of the year"))

        // Act
        viewModel.addPost()

        advanceUntilIdle()

        // Assert
        val uiState = viewModel.uiState.value
        assertTrue(uiState is AddPostUiState.Success)
        assertEquals("New game", (uiState as AddPostUiState.Success).post.title)

        coVerify { postRepo.addPost(any()) }
    }

    @Test
    fun `addPost shows no network error when offline`() = runTest {
        coEvery { fakeNetwork.isNetworkAvailable() } returns false
        viewModel.eventsFlow.test {

            viewModel.addPost()

            val event = awaitItem()
            assertTrue(event is Event.ShowToast)
        }

        coVerify(exactly = 0) { postRepo.addPost(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `addPost emits Generic error when repository throws`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        coEvery { fakeNetwork.isNetworkAvailable() } returns true
        coEvery { postRepo.addPost(any()) } throws Exception("DB error")

        viewModel.onAction(FormEvent.TitleChanged("New game"))

        // Act
        viewModel.addPost()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value

        assertTrue(state is AddPostUiState.Error.Generic)
        coVerify { postRepo.addPost(any()) }
    }


}