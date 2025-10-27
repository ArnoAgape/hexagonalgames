package com.openclassrooms.hexagonal.games.profileTest

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.openclassrooms.hexagonal.games.MainDispatcherRule
import com.openclassrooms.hexagonal.games.TestUtils
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import com.openclassrooms.hexagonal.games.domain.model.User
import com.openclassrooms.hexagonal.games.screen.profile.ProfileViewModel
import com.openclassrooms.hexagonal.games.ui.utils.NetworkUtils
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var viewModel: ProfileViewModel
    private lateinit var postRepo: PostRepository
    private lateinit var userRepo: UserRepository
    private lateinit var fakeNetwork: NetworkUtils

    @Before
    fun setup() {
        userRepo = mockk()
        postRepo = mockk()
        fakeNetwork = mockk()

        every { userRepo.observeCurrentUser() } returns flowOf(null)
        every { userRepo.isUserSignedIn() } returns flowOf(true)

        viewModel = ProfileViewModel(userRepo)
    }

    @After
    fun tearDown() {
        viewModel.viewModelScope.cancel()
        clearAllMocks()
    }

    @Test
    fun `user flow updates when repository emits new user`() = runTest {
        // Arrange
        val fakeUser = TestUtils.fakeUser("1")
        val userFlow = MutableSharedFlow<User?>()

        every { userRepo.observeCurrentUser() } returns userFlow

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