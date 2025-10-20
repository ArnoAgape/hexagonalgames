package com.openclassrooms.hexagonal.games.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.openclassrooms.hexagonal.games.screen.Screen
import com.openclassrooms.hexagonal.games.screen.addComment.AddCommentScreen
import com.openclassrooms.hexagonal.games.screen.addComment.AddCommentViewModel
import com.openclassrooms.hexagonal.games.screen.addPost.AddPostScreen
import com.openclassrooms.hexagonal.games.screen.addPost.AddPostViewModel
import com.openclassrooms.hexagonal.games.screen.detailPost.DetailPostUiState
import com.openclassrooms.hexagonal.games.screen.detailPost.DetailScreen
import com.openclassrooms.hexagonal.games.screen.detailPost.DetailPostViewModel
import com.openclassrooms.hexagonal.games.screen.homefeed.HomefeedScreen
import com.openclassrooms.hexagonal.games.screen.homefeed.HomefeedViewModel
import com.openclassrooms.hexagonal.games.screen.profile.ProfileScreen
import com.openclassrooms.hexagonal.games.screen.profile.ProfileViewModel
import com.openclassrooms.hexagonal.games.screen.profile.rememberSignInLauncher
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
    val profileViewModel: ProfileViewModel = hiltViewModel()

    NavHost(
        navController = navHostController,
        startDestination = Screen.Homefeed.route
    ) {

        composable(route = Screen.Homefeed.route) {
            val signInLauncher = rememberSignInLauncher(
                navController = navHostController,
                showMessage = showMessage,
                profileViewModel = profileViewModel
            )

            HomefeedScreen(
                homeViewModel = hiltViewModel<HomefeedViewModel>(),
                onFABClick = {
                    navHostController.navigate(Screen.AddPost.route)
                },
                onSettingsClick = {
                    navHostController.navigate(Screen.Settings.route)
                },
                onProfileClick = {
                    if (profileViewModel.isSignedIn.value) {
                        navHostController.navigate(Screen.Profile.route)
                    } else {
                        signInLauncher()
                    }
                },
                onPostClick = { post ->
                    navHostController.navigate(Screen.DetailPost.createRoute(post.id))
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
                viewModel = profileViewModel,
                onBackClick = { navHostController.navigateUp() }
            )
        }
        composable(
            route = Screen.DetailPost.route + "/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) {
            val signInLauncher = rememberSignInLauncher(
                navController = navHostController,
                showMessage = showMessage,
                profileViewModel = profileViewModel
            )

            val profileViewModel: ProfileViewModel = hiltViewModel()
            val detailViewModel: DetailPostViewModel = hiltViewModel()
            DetailScreen(
                viewModel = hiltViewModel<DetailPostViewModel>(),
                onBackClick = { navHostController.navigateUp() },
                onFABClick = {
                    val uiState = detailViewModel.uiPostState.value
                    if (uiState is DetailPostUiState.Success) {
                        val post = uiState.post
                        if (profileViewModel.isSignedIn.value) {
                            navHostController.navigate(Screen.AddComment.createRoute(post.id))
                        } else {
                            signInLauncher()
                        }
                    }
                }
            )
        }
        composable(
            route = Screen.AddComment.route + "/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = checkNotNull(backStackEntry.arguments?.getString("postId"))
            AddCommentScreen(
                viewModel = hiltViewModel<AddCommentViewModel>(),
                postId = postId,
                onBackClick = { navHostController.navigateUp() },
                onSaveClick = { navHostController.navigateUp() }
            )
        }
    }
}

