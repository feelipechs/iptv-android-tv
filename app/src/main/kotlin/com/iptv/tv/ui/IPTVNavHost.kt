package com.iptv.tv.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.iptv.tv.domain.model.ContentType
import com.iptv.tv.ui.screens.favorites.FavoritesScreen
import com.iptv.tv.ui.screens.home.HomeScreen
import com.iptv.tv.ui.screens.login.LoginScreen
import com.iptv.tv.ui.screens.login.LoginViewModel
import com.iptv.tv.ui.screens.player.PlayerScreen

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object Player : Screen("player/{streamUrl}") {
        fun route(encodedUrl: String) = "player/$encodedUrl"
    }
    data object Favorites : Screen("favorites")
}

@Composable
fun IPTVNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            val viewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onStreamSelected = { stream ->
                    val encodedUrl = java.net.URLEncoder.encode(stream.streamUrl, "UTF-8")
                    navController.navigate(Screen.Player.route(encodedUrl))
                },
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorites.route)
                }
            )
        }

        composable(
            route = Screen.Player.route,
            arguments = listOf(navArgument("streamUrl") { type = NavType.StringType })
        ) { backStack ->
            val encodedUrl = backStack.arguments?.getString("streamUrl") ?: return@composable
            val streamUrl = java.net.URLDecoder.decode(encodedUrl, "UTF-8")
            PlayerScreen(
                streamUrl = streamUrl,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onStreamSelected = { stream ->
                    val encodedUrl = java.net.URLEncoder.encode(stream.streamUrl, "UTF-8")
                    navController.navigate(Screen.Player.route(encodedUrl))
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}