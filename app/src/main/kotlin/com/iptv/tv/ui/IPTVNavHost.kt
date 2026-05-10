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
import com.iptv.tv.ui.screens.category.CategoryScreen
import com.iptv.tv.ui.screens.detail.DetailScreen
import com.iptv.tv.ui.screens.detail.SeriesDetailScreen
import com.iptv.tv.ui.screens.favorites.FavoritesScreen
import com.iptv.tv.ui.screens.home.HomeScreen
import com.iptv.tv.ui.screens.login.LoginScreen
import com.iptv.tv.ui.screens.login.LoginViewModel
import com.iptv.tv.ui.screens.player.PlayerScreen
import com.iptv.tv.ui.screens.settings.EditCredentialsScreen
import com.iptv.tv.ui.screens.settings.SettingsScreen
import com.iptv.tv.ui.screens.settings.SettingsViewModel
import com.iptv.tv.ui.screens.stream.StreamScreen
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object LoginEdit : Screen("login_edit")
    data object Home : Screen("home")
    data object Category : Screen("category/{type}") {
        fun route(type: ContentType): String = "category/${type.name}"
    }
    data object Stream : Screen("stream/{categoryId}/{type}") {
        fun route(categoryId: String, type: ContentType): String {
            val encodedCategoryId = URLEncoder.encode(categoryId, "UTF-8").replace("+", "%20")
            return "stream/$encodedCategoryId/${type.name}"
        }
    }
    data object Player : Screen("player/{streamId}/{streamUrl}/{streamName}/{streamType}/{startPosition}/{seriesId}/{posterUrl}/{episodeSeason}/{episodeNum}/{episodeTitle}") {
        fun route(
            streamId: String,
            encodedUrl: String,
            encodedName: String,
            encodedStreamType: String,
            startPosition: Long = 0L,
            seriesId: String = "",
            posterUrl: String = "",
            episodeSeason: String = "",
            episodeNum: String = "",
            episodeTitle: String = ""
        ) = "player/$streamId/$encodedUrl/$encodedName/$encodedStreamType/$startPosition/$seriesId/$posterUrl/$episodeSeason/$episodeNum/$episodeTitle"
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
            val encodedStreamId = URLEncoder.encode(streamId, "UTF-8").replace("+", "%20")
            val encodedStreamUrl = URLEncoder.encode(streamUrl, "UTF-8").replace("+", "%20")
            val encodedStreamName = URLEncoder.encode(streamName, "UTF-8").replace("+", "%20")
            val encodedPosterUrl = URLEncoder.encode(posterUrl ?: "", "UTF-8").replace("+", "%20")
            val encodedCategoryId = URLEncoder.encode(categoryId, "UTF-8").replace("+", "%20")
            val encodedContentType = URLEncoder.encode(contentType.name, "UTF-8").replace("+", "%20")
            return "detail/$encodedStreamId/$encodedStreamUrl/$encodedStreamName/$encodedPosterUrl/$encodedCategoryId/$encodedContentType"
        }
    }
    data object SeriesDetail : Screen("series_detail/{streamId}/{streamUrl}/{streamName}/{posterUrl}/{categoryId}") {
        fun route(
            streamId: String,
            streamUrl: String,
            streamName: String,
            posterUrl: String?,
            categoryId: String
        ): String {
            val encodedStreamId = URLEncoder.encode(streamId, "UTF-8").replace("+", "%20")
            val encodedStreamUrl = URLEncoder.encode(streamUrl, "UTF-8").replace("+", "%20")
            val encodedStreamName = URLEncoder.encode(streamName, "UTF-8").replace("+", "%20")
            val encodedPosterUrl = URLEncoder.encode(posterUrl ?: "", "UTF-8").replace("+", "%20")
            val encodedCategoryId = URLEncoder.encode(categoryId, "UTF-8").replace("+", "%20")
            return "series_detail/$encodedStreamId/$encodedStreamUrl/$encodedStreamName/$encodedPosterUrl/$encodedCategoryId"
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
                onNavigateToCategory = { type ->
                    navController.navigate(Screen.Category.route(type))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Category.route,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val typeStr = backStackEntry.arguments?.getString("type") ?: "LIVE"
            val type = try { ContentType.valueOf(typeStr) } catch (_: Exception) { ContentType.LIVE }
            CategoryScreen(
                type = type,
                onNavigateToStream = { categoryId ->
                    navController.navigate(Screen.Stream.route(categoryId, type))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Stream.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")?.decodeUrl() ?: ""
            val typeStr = backStackEntry.arguments?.getString("type") ?: "LIVE"
            val type = try { ContentType.valueOf(typeStr) } catch (_: Exception) { ContentType.LIVE }
            StreamScreen(
                categoryId = categoryId,
                type = type,
                onStreamSelected = { stream ->
                    when (stream.type) {
                        ContentType.VOD -> {
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
                        }
                        ContentType.SERIES -> {
                            android.util.Log.d("NavHost", "Navegando SERIES: streamId=${stream.id}, streamUrl=${stream.streamUrl}")
                            navController.navigate(
                                Screen.SeriesDetail.route(
                                    streamId = stream.id,
                                    streamUrl = stream.streamUrl,
                                    streamName = stream.name,
                                    posterUrl = stream.posterUrl,
                                    categoryId = stream.categoryId
                                )
                            )
                        }
            else -> {
                        navController.navigate(
                            Screen.Player.route(
                                stream.id.encodeUrl(),
                                stream.streamUrl.encodeUrl(),
                                stream.name.encodeUrl(),
                                stream.type.name.encodeUrl(),
                                posterUrl = (stream.posterUrl ?: "").encodeUrl()
                            )
                        )
                    }
                    }
                },
    onPlayEpisode = { episodeId, episodeUrl, episodeName, startPosition, seriesId, seriesPosterUrl, season, episodeNum ->
                navController.navigate(
                    Screen.Player.route(
                        episodeId.encodeUrl(),
                        episodeUrl.encodeUrl(),
                        episodeName.encodeUrl(),
                        ContentType.SERIES.name.encodeUrl(),
                        startPosition,
                        seriesId.encodeUrl(),
                        seriesPosterUrl.encodeUrl(),
                        episodeSeason = season.encodeUrl(),
                        episodeNum = episodeNum.encodeUrl(),
                        episodeTitle = episodeName.encodeUrl()
                    )
                )
            },
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = Screen.Player.route,
        arguments = listOf(
            navArgument("streamId") { type = NavType.StringType },
            navArgument("streamUrl") { type = NavType.StringType },
            navArgument("streamName") { type = NavType.StringType },
            navArgument("streamType") { type = NavType.StringType; defaultValue = "LIVE" },
        navArgument("startPosition") { type = NavType.LongType; defaultValue = 0L },
        navArgument("seriesId") { type = NavType.StringType; defaultValue = "" },
        navArgument("posterUrl") { type = NavType.StringType; defaultValue = "" },
        navArgument("episodeSeason") { type = NavType.StringType; defaultValue = "" },
        navArgument("episodeNum") { type = NavType.StringType; defaultValue = "" },
        navArgument("episodeTitle") { type = NavType.StringType; defaultValue = "" }
    )
) { backStack ->
    val args = backStack.arguments
    val streamId = args?.getString("streamId")?.decodeUrl() ?: ""
    val streamUrl = args?.getString("streamUrl")?.decodeUrl() ?: ""
    val streamName = args?.getString("streamName")?.decodeUrl() ?: ""
    val streamTypeStr = args?.getString("streamType") ?: "LIVE"
        val startPosition = args?.getLong("startPosition") ?: 0L
        val streamType = try { ContentType.valueOf(streamTypeStr) } catch (_: Exception) { ContentType.LIVE }
    val seriesId = args?.getString("seriesId")?.decodeUrl() ?: ""
    val posterUrl = args?.getString("posterUrl")?.decodeUrl() ?: ""
    val episodeSeason = args?.getString("episodeSeason")?.decodeUrl() ?: ""
    val episodeNum = args?.getString("episodeNum")?.decodeUrl() ?: ""
    val episodeTitle = args?.getString("episodeTitle")?.decodeUrl() ?: ""
    PlayerScreen(
        streamId = streamId,
        streamUrl = streamUrl,
        streamName = streamName,
        streamType = streamType,
        startPosition = startPosition,
        seriesId = seriesId,
        posterUrl = posterUrl,
        episodeSeason = episodeSeason,
        episodeNum = episodeNum,
        episodeTitle = episodeTitle,
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
            val streamId = args?.getString("streamId")?.decodeUrl() ?: ""
            val streamUrl = args?.getString("streamUrl")?.decodeUrl() ?: ""
            val streamName = args?.getString("streamName")?.decodeUrl() ?: ""
            val posterUrl = args?.getString("posterUrl")?.takeIf { it.isNotBlank() }?.decodeUrl()
            val categoryId = args?.getString("categoryId")?.decodeUrl() ?: ""
            val contentTypeStr = args?.getString("contentType") ?: "VOD"
            val contentType = try { ContentType.valueOf(contentTypeStr) } catch (_: Exception) { ContentType.VOD }

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
            onPlay = { playStream, startPosition ->
                navController.navigate(
                    Screen.Player.route(
                        playStream.id.encodeUrl(),
                        playStream.streamUrl.encodeUrl(),
                        playStream.name.encodeUrl(),
                        playStream.type.name.encodeUrl(),
                        startPosition,
                        posterUrl = (playStream.posterUrl ?: "").encodeUrl()
                    )
                )
            }
            )
        }

        composable(
            route = Screen.SeriesDetail.route,
            arguments = listOf(
                navArgument("streamId") { type = NavType.StringType },
                navArgument("streamUrl") { type = NavType.StringType },
                navArgument("streamName") { type = NavType.StringType },
                navArgument("posterUrl") { type = NavType.StringType },
                navArgument("categoryId") { type = NavType.StringType }
            )
        ) { backStack ->
            val args = backStack.arguments
            val streamId = args?.getString("streamId")?.decodeUrl() ?: ""
            val streamUrl = args?.getString("streamUrl")?.decodeUrl() ?: ""
            val streamName = args?.getString("streamName")?.decodeUrl() ?: ""
            val posterUrl = args?.getString("posterUrl")?.takeIf { it.isNotBlank() }?.decodeUrl()
            val categoryId = args?.getString("categoryId")?.decodeUrl() ?: ""

            val stream = Stream(
                id = streamId,
                name = streamName,
                categoryId = categoryId,
                type = ContentType.SERIES,
                streamUrl = streamUrl,
                posterUrl = posterUrl
            )

        SeriesDetailScreen(
            stream = stream,
            onPlayEpisode = { episodeId, episodeUrl, episodeName, startPosition, seriesId, seriesPosterUrl, season, episodeNum ->
                navController.navigate(
                    Screen.Player.route(
                        episodeId.encodeUrl(),
                        episodeUrl.encodeUrl(),
                        episodeName.encodeUrl(),
                        ContentType.SERIES.name.encodeUrl(),
                        startPosition,
                        seriesId.encodeUrl(),
                        seriesPosterUrl.encodeUrl(),
                        episodeSeason = season.encodeUrl(),
                        episodeNum = episodeNum.encodeUrl(),
                        episodeTitle = episodeName.encodeUrl()
                    )
                )
            }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onStreamSelected = { stream ->
                    when (stream.type) {
                        ContentType.VOD -> navController.navigate(
                            Screen.Detail.route(stream.id, stream.streamUrl, stream.name, stream.posterUrl, stream.categoryId, stream.type)
                        )
                        ContentType.SERIES -> navController.navigate(
                            Screen.SeriesDetail.route(stream.id, stream.streamUrl, stream.name, stream.posterUrl, stream.categoryId)
                        )
        else -> navController.navigate(
                Screen.Player.route(stream.id.encodeUrl(), stream.streamUrl.encodeUrl(), stream.name.encodeUrl(), stream.type.name.encodeUrl(), posterUrl = (stream.posterUrl ?: "").encodeUrl())
            )
                    }
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
                }
            )
        }

        composable(Screen.EditCredentials.route) {
            val viewModel: SettingsViewModel = hiltViewModel()
            val credentials by viewModel.credentials.collectAsStateWithLifecycle()

            credentials?.let { cred ->
                EditCredentialsScreen(
                    credentials = cred,
                    onNavigateBack = { navController.popBackStack() },
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
