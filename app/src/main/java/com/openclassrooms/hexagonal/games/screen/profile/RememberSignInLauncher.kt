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

/**
 * Remembers and returns a lambda function that launches the Firebase Authentication
 * sign-in flow using FirebaseUI.
 *
 * This composable encapsulates the logic for initiating a sign-in intent and
 * handling its result, including successful login, user cancellation, and errors.
 *
 * When authentication succeeds:
 * - The [ProfileViewModel.syncUserWithFirestore] function is called to ensure the
 *   user's data is synchronized with Firestore.
 * - A success message is shown via [showMessage].
 * - Navigation returns to the Homefeed screen, clearing the back stack.
 *
 * When authentication fails or is canceled:
 * - Appropriate log messages and user-facing messages are displayed.
 *
 * @param navController The [NavHostController] used for app navigation.
 * @param showMessage A lambda used to display toast or snackbar messages to the user.
 * @param profileViewModel The [ProfileViewModel] used to sync user data post sign-in.
 *
 * @return A lambda function that launches the Firebase sign-in flow when invoked.
 */
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
            Log.d("Auth", "Successfully connected: ${user?.email ?: "Unknown user"}")

            profileViewModel.syncUserWithFirestore()

            showMessage("Connection successful")

            navController.navigate(Screen.Homefeed.route) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        } else {
            // Failed or canceled by the user
            if (response == null) {
                Log.w("Auth", "Connection canceled by user.")
                showMessage("Connection canceled.")
            } else {
                // Error
                val errorCode = response.error?.errorCode
                Log.w("Auth", "Connection failed. Error code: $errorCode", response.error)
                showMessage("Connection failed. Please try again.")
            }
        }
    }

    val providers = remember { listOf(AuthUI.IdpConfig.EmailBuilder().build()) }
    val signInIntent = remember(providers) {
        AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setTheme(R.style.Theme_HexagonalGames_Login)
            .setAvailableProviders(providers)
            .build()
    }

    return {
        signInLauncher.launch(signInIntent)
    }
}