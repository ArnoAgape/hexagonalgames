package com.openclassrooms.hexagonal.games.data.service.user

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.openclassrooms.hexagonal.games.domain.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firebase-based implementation of the [UserApi] interface.
 *
 * This class provides methods to manage authentication state and user data
 * through Firebase Authentication and Firestore. It handles sign-in state
 * observation, user document synchronization, and account deletion.
 *
 * The user profile data is stored in the `"users"` collection in Firestore,
 * with the document ID matching the Firebase Authentication user ID.
 */
class FirebaseUserApi : UserApi {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    /**
     * Converts a [FirebaseUser] object into a domain-level [User].
     */
    private fun FirebaseUser.toDomain(): User = User(
        id = uid,
        displayName = displayName,
        email = email,
        photoUrl = photoUrl?.toString()
    )

    /**
     * Retrieves the currently authenticated [User], if any.
     *
     * @return The current [User], or `null` if no user is signed in.
     */
    override suspend fun getCurrentUser(): User? = auth.currentUser?.toDomain()

    /**
     * Observes authentication state changes in real time.
     *
     * @return A [Flow] emitting the current [User] or `null` on sign-out.
     */
    override fun observeCurrentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toDomain())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Ensures that the current authenticated user exists in Firestore.
     * If the user document does not exist, it is created automatically.
     *
     * @return A [Result] indicating success or failure.
     */
    override suspend fun ensureUserInFirestore(): Result<Unit> {
        val firebaseUser = auth.currentUser ?: return Result.failure(Exception("User not signed in"))
        val user = firebaseUser.toDomain()
        return try {
            val doc = usersCollection.document(user.id).get().await()
            if (!doc.exists()) {
                usersCollection.document(user.id).set(user).await()
                Log.d("UserRepository", "Document Firestore created for ${user.email}")
            } else {
                Log.d("UserRepository", "Document Firestore already exists for ${user.email}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Signs the current user out of Firebase Authentication.
     *
     * @return A [Result] representing the outcome of the operation.
     */
    override fun signOut(): Result<Unit> = try {
        auth.signOut()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Observes whether a user is currently signed in.
     *
     * @return A [Flow] emitting `true` if signed in, or `false` otherwise.
     */
    override fun isUserSignedIn(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Deletes the currently authenticated user and their Firestore document.
     *
     * @return A [Result] indicating whether the deletion was successful.
     */
    override suspend fun deleteUser(): Result<Unit> = try {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("No user signed in"))
        usersCollection.document(currentUser.uid).delete().await()
        currentUser.delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
    Result.failure(e)}
}
