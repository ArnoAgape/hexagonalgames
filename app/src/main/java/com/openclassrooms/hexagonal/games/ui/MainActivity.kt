package com.openclassrooms.hexagonal.games.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.openclassrooms.hexagonal.games.R
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.openclassrooms.hexagonal.games.screen.Screen
import com.openclassrooms.hexagonal.games.screen.addComment.AddCommentScreen
import com.openclassrooms.hexagonal.games.screen.addComment.AddCommentViewModel
import com.openclassrooms.hexagonal.games.screen.addPost.AddPostScreen
import com.openclassrooms.hexagonal.games.screen.addPost.AddPostViewModel
import com.openclassrooms.hexagonal.games.screen.detailPost.DetailScreen
import com.openclassrooms.hexagonal.games.screen.detailPost.DetailPostViewModel
import com.openclassrooms.hexagonal.games.screen.homefeed.HomefeedScreen
import com.openclassrooms.hexagonal.games.screen.homefeed.HomefeedViewModel
import com.openclassrooms.hexagonal.games.screen.profile.ProfileScreen
import com.openclassrooms.hexagonal.games.screen.profile.ProfileViewModel
import com.openclassrooms.hexagonal.games.screen.settings.SettingsScreen
import com.openclassrooms.hexagonal.games.screen.settings.SettingsViewModel
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Main activity for the application. This activity serves as the entry point and container for the navigation
 * fragment. It handles setting up the toolbar, navigation controller, and action bar behavior.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            val showMessage: (String) -> Unit = { message ->
                scope.launch {
                    snackbarHostState.showSnackbar(message)
                }
            }

            HexagonalGamesTheme {
                HexagonalGamesNavHost(
                    navHostController = navController,
                    showMessage = showMessage
                )
            }
        }
    }
}

@Composable
fun HexagonalGamesNavHost(
    navHostController: NavHostController,
    showMessage: (String) -> Unit
) {
    NavHost(
        navController = navHostController,
        startDestination = Screen.Homefeed.route
    ) {


        composable(route = Screen.Homefeed.route) {
            val signInLauncher = rememberSignInLauncher(
                navController = navHostController,
                showMessage = showMessage
            )
            val profileViewModel: ProfileViewModel = hiltViewModel()
            HomefeedScreen(
                homeViewModel = hiltViewModel<HomefeedViewModel>(),
                onFABClick = {
                    if (profileViewModel.isSignedIn) {
                        navHostController.navigate(Screen.AddPost.route)
                    } else {
                        signInLauncher()
                    }
                },
                onSettingsClick = {
                    navHostController.navigate(Screen.Settings.route)
                },
                onProfileClick = {
                    if (profileViewModel.isSignedIn) {
                        navHostController.navigate(Screen.Profile.route)
                    } else {
                        signInLauncher()
                    }
                },
                onPostClick = { post ->
                    navHostController.navigate("detail/${post.id}")
                }
            )
        }
        composable(route = Screen.AddPost.route) {
            AddPostScreen(
                viewModel = hiltViewModel<AddPostViewModel>(),
                onBackClick = { navHostController.navigateUp() },
                onSaveClick = { navHostController.navigateUp() }
            )
        }
        composable(route = Screen.Settings.route) {
            SettingsScreen(
                viewModel = hiltViewModel<SettingsViewModel>(),
                onBackClick = { navHostController.navigateUp() }
            )
        }
        composable(route = Screen.Profile.route) {
            ProfileScreen(
                viewModel = hiltViewModel<ProfileViewModel>(),
                onBackClick = { navHostController.navigateUp() }
            )
        }
        composable(route = Screen.DetailPost.route) {
            val signInLauncher = rememberSignInLauncher(
                navController = navHostController,
                showMessage = showMessage
            )
            val profileViewModel: ProfileViewModel = hiltViewModel()
            DetailScreen(
                viewModel = hiltViewModel<DetailPostViewModel>(),
                onBackClick = { navHostController.navigateUp() },
                onFABClick = {
                    if (profileViewModel.isSignedIn) {
                        navHostController.navigate(Screen.AddComment.route)
                    } else {
                        signInLauncher()
                    }
                }
            )
        }
        composable(route = Screen.AddComment.route) {
            AddCommentScreen(
                viewModel = hiltViewModel<AddCommentViewModel>(),
                onBackClick = { navHostController.navigateUp() },
                onSaveClick = { navHostController.navigateUp() }
            )
        }
    }
}

/**
 * Composable qui prépare et retourne un lanceur pour le flux d'authentification FirebaseUI.
 * Gère le résultat de la tentative de connexion, affiche des messages à l'utilisateur
 * et navigue en conséquence.
 *
 * @param navController Le contrôleur de navigation pour gérer les transitions d'écran.
 * @param showMessage Une fonction lambda pour afficher des messages (par exemple, via un Snackbar).
 * @return Une fonction lambda () -> Unit qui, lorsqu'elle est appelée, lance le flux d'authentification.
 */
@Composable
fun rememberSignInLauncher(
    navController: NavHostController,
    showMessage: (String) -> Unit
): () -> Unit {
    val firebaseAuth = FirebaseAuth.getInstance()

    // Le lanceur pour l'activité de connexion FirebaseUI.
    val signInLauncher: ActivityResultLauncher<Intent> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val response = IdpResponse.fromResultIntent(result.data)

        if (result.resultCode == Activity.RESULT_OK) {
            // Connexion réussie
            val user = firebaseAuth.currentUser
            Log.d("Auth", "Connexion réussie : ${user?.email ?: "Utilisateur inconnu"}")
            showMessage("Connexion réussie !") // Message pour l'utilisateur

            navController.navigate(Screen.Homefeed.route) {
                popUpTo(navController.graph.startDestinationId) { // Ou popUpTo(Screen.Homefeed.route)
                    inclusive = true
                }
                launchSingleTop = true // Évite de recréer Homefeed s'il est déjà au sommet
            }
        } else {
            // Connexion échouée ou annulée par l'utilisateur
            if (response == null) {
                Log.w("Auth", "Connexion annulée par l'utilisateur.")
                showMessage("Connexion annulée.")
            } else {
                // Une erreur s'est produite lors de la connexion
                val errorCode = response.error?.errorCode
                Log.w("Auth", "Échec de la connexion. Code d'erreur : $errorCode", response.error)
                showMessage("Échec de la connexion. Veuillez réessayer.")
            }
        }
    }

    // `remember` pour les fournisseurs et l'intention de connexion afin d'éviter de les recréer
    // inutilement à chaque recomposition si les dépendances ne changent pas.
    // Bien que pour ce cas spécifique, l'impact soit minime car cela n'est utilisé que
    // lors de l'appel de la fonction retournée.
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

