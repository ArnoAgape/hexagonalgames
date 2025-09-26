package com.openclassrooms.hexagonal.games.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.hexagonal.games.domain.model.User
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    private fun FirebaseUser.toDomain(): User {
        return User(
            id = uid,
            displayName = displayName,
            email = email,
            photoUrl = photoUrl?.toString()
        )
    }

    fun getCurrentUser(): User? {
        return auth.currentUser?.toDomain()
    }

    fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun deleteUser(): Result<Unit> {
        return try {
            auth.currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}