package com.iptv.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.iptv.tv.ui.screens.settings.EditCredentialsScreen
import com.iptv.tv.ui.screens.settings.SettingsScreen
import com.iptv.tv.ui.screens.settings.SettingsViewModel

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object LoginEdit : Screen("login_edit")
    data object Home : Screen("home")
    data object Player : Screen("player/{streamUrl}") {
        fun route(encodedUrl: String) = "player/$encodedUrl"
    }
    data object Favorites : Screen("favorites")
    data object Settings : Screen("settings")
    data object EditCredentials : Screen("edit_credentials")
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
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
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

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.LoginEdit.route)
                },
                onNavigateToEdit = {
                    navController.navigate(Screen.EditCredentials.route)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.EditCredentials.route) {
            val viewModel: SettingsViewModel = hiltViewModel()
            val credentials by viewModel.credentials.collectAsStateWithLifecycle()

            credentials?.let { cred ->
                EditCredentialsScreen(
                    credentials = cred,
                    onBack = { navController.popBackStack() },
                    onSave = { updatedCreds ->
                        viewModel.saveCredentials(updatedCreds)
                    }
                )
            }
        }

        composable(Screen.LoginEdit.route) {
            val viewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}