package com.openclassrooms.hexagonal.games.data.service.user

import com.openclassrooms.hexagonal.games.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserApi {
    fun getCurrentUser(): User?
    suspend fun ensureUserInFirestore(): Result<Unit>
    fun signOut(): Result<Unit>
    fun isUserSignedIn(): Flow<Boolean>
    suspend fun deleteUser(): Result<Unit>
}