package com.openclassrooms.hexagonal.games.screen

sealed class Screen(val route: String) {
    data object Homefeed : Screen("homefeed")

    data object AddPost : Screen("addPost")

    data object Settings : Screen("settings")

    data object Profile : Screen("profile")

    data object DetailPost : Screen("detail") {
        fun createRoute(postId: String) = "detail/$postId"
    }

    data object AddComment : Screen("addComment"){
        fun createRoute(postId: String) = "addComment/$postId"
    }
}