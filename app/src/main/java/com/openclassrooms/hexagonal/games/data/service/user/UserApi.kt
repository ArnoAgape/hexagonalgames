package com.openclassrooms.hexagonal.games.data.service.user

import com.openclassrooms.hexagonal.games.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Defines the contract for interacting with user data and authentication state.
 * This interface abstracts away the details of user management such as
 * authentication, Firestore persistence, and observing sign-in status.
 */
interface UserApi {

    /**
     * Retrieves the currently authenticated user.
     *
     * @return The [User] object if a user is signed in, or `null` otherwise.
     */
    suspend fun getCurrentUser(): User?

    /**
     * Observes the currently authenticated user as a [Flow].
     * Emits updates whenever the user data changes (e.g., profile update or sign-out).
     *
     * @return A [Flow] emitting the current [User] or `null` if no user is signed in.
     */
    fun observeCurrentUser(): Flow<User?>

    /**
     * Ensures that the authenticated user exists in Firestore.
     * Creates or updates the user document as needed.
     *
     * @return A [Result] indicating success or failure of the operation.
     */
    suspend fun ensureUserInFirestore(): Result<Unit>

    /**
     * Signs the current user out of the application.
     *
     * @return A [Result] representing the outcome of the sign-out operation.
     */
    fun signOut(): Result<Unit>

    /**
     * Observes the user's authentication state as a [Flow].
     *
     * @return A [Flow] emitting `true` if the user is signed in, or `false` otherwise.
     */
    fun isUserSignedIn(): Flow<Boolean>

    /**
     * Permanently deletes the currently authenticated user account.
     *
     * @return A [Result] indicating whether the deletion was successful or not.
     */
    suspend fun deleteUser(): Result<Unit>
}