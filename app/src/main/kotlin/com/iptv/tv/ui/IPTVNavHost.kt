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
import com.iptv.tv.domain.model.Stream
import com.iptv.tv.ui.screens.detail.DetailScreen
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
    data object Detail : Screen("detail/{streamId}/{streamUrl}/{streamName}/{posterUrl}/{categoryId}/{contentType}") {
        fun route(
            streamId: String,
            streamUrl: String,
            streamName: String,
            posterUrl: String?,
            categoryId: String,
            contentType: ContentType
        ): String {
            val encodedStreamId = java.net.URLEncoder.encode(streamId, "UTF-8")
            val encodedStreamUrl = java.net.URLEncoder.encode(streamUrl, "UTF-8")
            val encodedStreamName = java.net.URLEncoder.encode(streamName, "UTF-8")
            val encodedPosterUrl = java.net.URLEncoder.encode(posterUrl ?: "", "UTF-8")
            val encodedCategoryId = java.net.URLEncoder.encode(categoryId, "UTF-8")
            val encodedContentType = java.net.URLEncoder.encode(contentType.name, "UTF-8")
            return "detail/$encodedStreamId/$encodedStreamUrl/$encodedStreamName/$encodedPosterUrl/$encodedCategoryId/$encodedContentType"
        }
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
                    if (stream.type == ContentType.VOD) {
                        navController.navigate(
                            Screen.Detail.route(
                                streamId = stream.id,
                                streamUrl = stream.streamUrl,
                                streamName = stream.name,
                                posterUrl = stream.posterUrl,
                                categoryId = stream.categoryId,
                                contentType = stream.type
                            )
                        )
                    } else {
                        val encodedUrl = java.net.URLEncoder.encode(stream.streamUrl, "UTF-8")
                        navController.navigate(Screen.Player.route(encodedUrl))
                    }
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

        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("streamId") { type = NavType.StringType },
                navArgument("streamUrl") { type = NavType.StringType },
                navArgument("streamName") { type = NavType.StringType },
                navArgument("posterUrl") { type = NavType.StringType },
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("contentType") { type = NavType.StringType }
            )
        ) { backStack ->
            val args = backStack.arguments
            val streamId = args?.getString("streamId") ?: ""
            val streamUrl = java.net.URLDecoder.decode(args?.getString("streamUrl") ?: "", "UTF-8")
            val streamName = java.net.URLDecoder.decode(args?.getString("streamName") ?: "", "UTF-8")
            val posterUrl = args?.getString("posterUrl")?.takeIf { it.isNotBlank() }?.let {
                java.net.URLDecoder.decode(it, "UTF-8")
            }
            val categoryId = args?.getString("categoryId") ?: ""
            val contentTypeStr = args?.getString("contentType") ?: "VOD"
            val contentType = try {
                ContentType.valueOf(contentTypeStr)
            } catch (e: Exception) {
                ContentType.VOD
            }

            val stream = Stream(
                id = streamId,
                name = streamName,
                categoryId = categoryId,
                type = contentType,
                streamUrl = streamUrl,
                posterUrl = posterUrl
            )

            DetailScreen(
                stream = stream,
                onPlay = { playStream ->
                    val encodedUrl = java.net.URLEncoder.encode(playStream.streamUrl, "UTF-8")
                    navController.navigate(Screen.Player.route(encodedUrl))
                },
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