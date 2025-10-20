package com.openclassrooms.hexagonal.games.screen.profile

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.screen.Screen

@Composable
fun rememberSignInLauncher(
    navController: NavHostController,
    showMessage: (String) -> Unit,
    profileViewModel: ProfileViewModel
): () -> Unit {
    val firebaseAuth = FirebaseAuth.getInstance()

    val signInLauncher: ActivityResultLauncher<Intent> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val response = IdpResponse.fromResultIntent(result.data)

        if (result.resultCode == Activity.RESULT_OK) {
            val user = firebaseAuth.currentUser
            Log.d("Auth", "Connexion réussie : ${user?.email ?: "Utilisateur inconnu"}")

            profileViewModel.syncUserWithFirestore()

            showMessage("Connexion réussie !")

            navController.navigate(Screen.Homefeed.route) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        } else {
            // Failed or canceled by the user
            if (response == null) {
                Log.w("Auth", "Connexion annulée par l'utilisateur.")
                showMessage("Connexion annulée.")
            } else {
                // Error
                val errorCode = response.error?.errorCode
                Log.w("Auth", "Échec de la connexion. Code d'erreur : $errorCode", response.error)
                showMessage("Échec de la connexion. Veuillez réessayer.")
            }
        }
    }

    val providers = remember { listOf(AuthUI.IdpConfig.EmailBuilder().build()) }
    val signInIntent = remember(providers) { // Recréer si `providers` change (peu probable ici)
        AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setTheme(R.style.Base_Theme_HexagonalGames) // Assurez-vous que ce style est bien défini
            .setAvailableProviders(providers)
            .build()
    }

    return {
        signInLauncher.launch(signInIntent)
    }
}