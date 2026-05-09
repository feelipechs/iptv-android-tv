package com.iptv.tv.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.iptv.tv.domain.model.ContentType

@Composable
fun HomeScreen(
    onNavigateToCategory: (ContentType) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val firstCardFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        firstCardFocus.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(48.dp)
        ) {
            Text(
                text = "IPTV",
                style = MaterialTheme.typography.displayLarge
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HomeCard(
                    title = "TV ao Vivo",
                    icon = Icons.Filled.Tv,
                    onClick = { onNavigateToCategory(ContentType.LIVE) },
                    modifier = Modifier.focusRequester(firstCardFocus)
                )
                HomeCard(
                    title = "Filmes",
                    icon = Icons.Filled.Movie,
                    onClick = { onNavigateToCategory(ContentType.VOD) }
                )
                HomeCard(
                    title = "Séries",
                    icon = Icons.Filled.VideoLibrary,
                    onClick = { onNavigateToCategory(ContentType.SERIES) }
                )
            }
        }

        Surface(
            onClick = onNavigateToSettings,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
            scale = ClickableSurfaceScale.None,
            colors = ClickableSurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedContentColor = MaterialTheme.colorScheme.onPrimary,
                pressedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                pressedContentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Configurações",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun HomeCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = modifier
            .size(220.dp, 140.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Surface(
            onClick = onClick,
            interactionSource = interactionSource,
            modifier = Modifier.fillMaxSize(),
            scale = ClickableSurfaceScale.None,
            colors = ClickableSurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedContentColor = MaterialTheme.colorScheme.onPrimary,
                pressedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                pressedContentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}
