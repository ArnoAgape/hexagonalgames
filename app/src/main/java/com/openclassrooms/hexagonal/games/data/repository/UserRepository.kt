package com.openclassrooms.hexagonal.games.data.repository

import com.openclassrooms.hexagonal.games.data.service.user.UserApi
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class UserRepository @Inject constructor(private val userApi: UserApi) {
    fun getCurrentUser() = userApi.getCurrentUser()
    suspend fun ensureUserInFirestore() = userApi.ensureUserInFirestore()
    fun signOut() = userApi.signOut()
    fun isUserSignedIn() = userApi.isUserSignedIn()
    suspend fun deleteUser() = userApi.deleteUser()
}