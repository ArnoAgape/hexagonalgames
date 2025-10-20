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

class FirebaseUserApi : UserApi {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    private fun FirebaseUser.toDomain(): User {
        return User(
            id = uid,
            displayName = displayName,
            email = email,
            photoUrl = photoUrl?.toString()
        )
    }

    override fun getCurrentUser(): User? {
        return auth.currentUser?.toDomain()
    }

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

    override fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isUserSignedIn(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }


    override suspend fun deleteUser(): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("No user signed in"))
            usersCollection.document(currentUser.uid).delete().await()
            currentUser.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}