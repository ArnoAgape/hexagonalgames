package com.openclassrooms.hexagonal.games.profileTest

import app.cash.turbine.test
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import com.openclassrooms.hexagonal.games.domain.model.User
import com.openclassrooms.hexagonal.games.screen.profile.ProfileViewModel
import com.openclassrooms.hexagonal.games.ui.utils.NetworkUtils
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var viewModel: ProfileViewModel
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

        viewModel = ProfileViewModel(userRepo)
    }

    @Test
    fun `user flow updates when repository emits new user`() = runTest {
        // Arrange
        val fakeUser = User("1", "Alice", "alice@mail.com")
        val userFlow = MutableSharedFlow<User?>()
        every { userRepo.observeCurrentUser() } returns userFlow
        every { userRepo.isUserSignedIn() } returns flowOf(true)

        // Act
        viewModel = ProfileViewModel(userRepo)

        // Assert
        viewModel.user.test {
            assertNull(awaitItem()) // initial null

            userFlow.emit(fakeUser)
            val emitted = awaitItem()
            assertEquals(fakeUser, emitted)
        }
    }

    @Test
    fun `syncUserWithFirestore calls ensureUserInFirestore in repository`() = runTest {
        // Arrange
        coEvery { userRepo.ensureUserInFirestore() } returns Result.success(Unit)
        every { userRepo.observeCurrentUser() } returns flowOf(null)
        every { userRepo.isUserSignedIn() } returns flowOf(true)

        viewModel = ProfileViewModel(userRepo)

        // Act
        viewModel.syncUserWithFirestore()

        // Assert
        coVerify(exactly = 1) { userRepo.ensureUserInFirestore() }
    }


}