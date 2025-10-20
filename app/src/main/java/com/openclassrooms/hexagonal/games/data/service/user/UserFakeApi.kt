package com.openclassrooms.hexagonal.games.data.service.user

import android.util.Log
import com.openclassrooms.hexagonal.games.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Fake API simulant la logique de FirebaseUserApi pour les tests ou le mode hors-ligne.
 */
class UserFakeApi : UserApi {

    private val users = mutableListOf(
        User("1", "Gerry", "gerry@example.com"),
        User("2", "Brenton", "brenton@example.com"),
        User("3", "Wally", "wally@example.com")
    )

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _isUserSignedIn = MutableStateFlow(false)
    val isUserSignedIn: StateFlow<Boolean> = _isUserSignedIn.asStateFlow()

    override fun getCurrentUser(): User? = _currentUser.value

    override suspend fun ensureUserInFirestore(): Result<Unit> {
        val user = _currentUser.value ?: return Result.failure(Exception("Utilisateur non connecté"))
        return try {
            if (users.none { it.id == user.id }) {
                users.add(user)
                Log.d("UserFakeApi", "Fake user ajouté : ${user.displayName}")
            } else {
                Log.d("UserFakeApi", "Fake user déjà existant : ${user.displayName}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UserFakeApi", "Erreur lors de l'ajout du fake user", e)
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

    suspend fun signInFakeUser(userId: String) {
        val user = users.find { it.id == userId }
        delay(300)
        _currentUser.value = user
    }
}
