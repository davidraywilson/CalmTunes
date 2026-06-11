package com.maxrave.simpmusic.einkui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.maxrave.domain.data.entities.PlaylistEntity
import com.maxrave.simpmusic.viewModel.LibraryViewModel

@Composable
fun PlaylistsScreenWrapper(viewModel: LibraryViewModel) {
    val favoritePlaylistsState by viewModel.favoritePlaylist.collectAsState()
    val localPlaylistsState by viewModel.yourLocalPlaylist.collectAsState()

    val mappedPlaylists = remember(favoritePlaylistsState, localPlaylistsState) {
        val mappedFavs = favoritePlaylistsState.data?.filterIsInstance<PlaylistEntity>()?.map {
            PlaylistUiModel(
                id = it.id,
                name = it.title,
                description = it.author,
                songCount = null 
            )
        } ?: emptyList()

        val mappedLocals = localPlaylistsState.data?.map {
            PlaylistUiModel(
                id = it.id.toString(),
                name = it.title,
                description = "Local",
                songCount = null
            )
        } ?: emptyList()

        mappedFavs + mappedLocals
    }

    PlaylistsScreen(
        playlists = mappedPlaylists,
        isInEditMode = false,
        onPlaylistClick = { /* TODO */ },
        onAddPlaylistClick = { /* TODO */ },
        onSelectionChanged = { /* TODO */ }
    )
}
