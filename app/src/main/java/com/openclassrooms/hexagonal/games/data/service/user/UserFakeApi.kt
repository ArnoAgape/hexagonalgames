package com.openclassrooms.hexagonal.games.data.service.user

import android.util.Log
import com.openclassrooms.hexagonal.games.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * A fake implementation of [UserApi] that simulates the behavior of [FirebaseUserApi]
 * for offline mode, previews, or unit testing.
 *
 * This object maintains a list of mock [User] instances and provides reactive
 * [StateFlow]s to observe the current user and authentication state.
 *
 * It can be used to test user-related logic without requiring a network connection
 * or interaction with Firebase services.
 *
 * - `users`: Contains a mutable list of fake users available for testing.
 * - `_currentUser`: Holds the currently "signed-in" fake user.
 * - `_isUserSignedIn`: Represents whether a fake user is currently signed in.
 *
 * The API methods mimic real-world operations such as ensuring the user exists
 * in the data source, signing out, or deleting the current user, while logging
 * corresponding actions for debugging purposes.
 */
object UserFakeApi : UserApi {

    val users = mutableListOf(
        User("1", "Gerry", "gerry@example.com"),
        User("2", "Brenton", "brenton@example.com"),
        User("3", "Wally", "wally@example.com")
    )

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _isUserSignedIn = MutableStateFlow(false)
    val isUserSignedIn: StateFlow<Boolean> = _isUserSignedIn.asStateFlow()

    override suspend fun getCurrentUser(): User? = _currentUser.value

    override fun observeCurrentUser(): Flow<User?> {
        return currentUser
    }

    override suspend fun ensureUserInFirestore(): Result<Unit> {
        val user = _currentUser.value ?: return Result.failure(Exception("User not signed in"))
        return try {
            if (users.none { it.id == user.id }) {
                users.add(user)
                Log.d("UserFakeApi", "Fake user added : ${user.displayName}")
            } else {
                Log.d("UserFakeApi", "Fake user already exists : ${user.displayName}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UserFakeApi", "Error while adding the fake user", e)
            Result.failure(e)
        }
    }

    override fun signOut(): Result<Unit> {
        return try {
            _currentUser.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isUserSignedIn(): Flow<Boolean> = isUserSignedIn

    override suspend fun deleteUser(): Result<Unit> {
        val user = _currentUser.value ?: return Result.failure(Exception("Aucun utilisateur connecté"))
        return try {
            users.removeIf { it.id == user.id }
            _currentUser.value = null
            Log.d("UserFakeApi", "Fake user supprimé : ${user.displayName}")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}