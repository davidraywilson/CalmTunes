package com.maxrave.simpmusic.einkui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.maxrave.domain.data.entities.AlbumEntity
import com.maxrave.simpmusic.viewModel.LibraryViewModel

@Composable
fun AlbumsScreenWrapper(viewModel: LibraryViewModel) {
    val favoritePlaylistsState by viewModel.favoritePlaylist.collectAsState()

    val mappedAlbums = remember(favoritePlaylistsState) {
        favoritePlaylistsState.data?.filterIsInstance<AlbumEntity>()?.map {
            AlbumUiModel(
                id = it.browseId,
                title = it.title,
                artist = it.artistName?.joinToString() ?: "",
                sourceType = it.type,
                releaseYear = it.year?.toIntOrNull()
            )
        } ?: emptyList()
    }

    AlbumsScreen(
        isAuthenticated = true,
        albums = mappedAlbums,
        isLoading = false,
        errorMessage = null,
        isSyncInProgress = false,
        hasAnySongs = true,
        onOpenStreamingSettingsClick = { /* TODO */ },
        onOpenLocalSettingsClick = { /* TODO */ },
        onAlbumClick = { /* TODO */ }
    )
}
