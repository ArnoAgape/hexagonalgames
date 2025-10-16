package com.openclassrooms.hexagonal.games.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.openclassrooms.hexagonal.games.domain.model.User
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val usersCollection = firestore.collection("users")

    // 🔹 Convertit un FirebaseUser en User (modèle de ton domaine)
    private fun FirebaseUser.toDomain(): User {
        return User(
            id = uid,
            displayName = displayName,
            email = email,
            photoUrl = photoUrl?.toString()
        )
    }

    // 🔹 Récupère l’utilisateur actuellement connecté depuis FirebaseAuth
    fun getCurrentUser(): User? {
        return auth.currentUser?.toDomain()
    }

    suspend fun ensureUserInFirestore(): Result<Unit> {
        val firebaseUser = auth.currentUser ?: return Result.failure(Exception("Utilisateur non connecté"))
        val user = firebaseUser.toDomain()
        return try {
            // Vérifie si le doc existe déjà
            val doc = usersCollection.document(user.id).get().await()
            if (!doc.exists()) {
                usersCollection.document(user.id).set(user).await()
                Log.d("UserRepository", "✅ Document Firestore créé pour ${user.email}")
            } else {
                Log.d("UserRepository", "ℹ️ Document Firestore déjà existant pour ${user.email}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepository", "❌ Erreur Firestore lors de ensureUserInFirestore", e)
            Result.failure(e)
        }
    }

    // 🔹 Déconnexion
    fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Vérifie si un utilisateur est connecté
    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }

    // 🔹 Supprime complètement l’utilisateur (Auth + Firestore)
    suspend fun deleteUser(): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("Aucun utilisateur connecté"))
            usersCollection.document(currentUser.uid).delete().await()
            currentUser.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}