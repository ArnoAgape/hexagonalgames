package com.openclassrooms.hexagonal.games.data.repository

import com.openclassrooms.hexagonal.games.data.service.user.UserApi
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Singleton
class UserRepository @Inject constructor(private val userApi: UserApi) {
    fun getCurrentUser() = userApi.getCurrentUser()
    suspend fun ensureUserInFirestore() = userApi.ensureUserInFirestore()
    fun signOut() = userApi.signOut()
    fun isUserSignedIn(): Flow<Boolean> = flowOf(userApi.isUserSignedIn())
    suspend fun deleteUser() = userApi.deleteUser()
}