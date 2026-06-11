package com.maxrave.simpmusic.einkui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.maxrave.simpmusic.viewModel.AlbumViewModel
import com.maxrave.simpmusic.viewModel.LocalPlaylistState

@Composable
fun AlbumDetailsScreenWrapper(
    browseId: String,
    viewModel: AlbumViewModel,
    navController: NavController
) {
    LaunchedEffect(browseId) {
        viewModel.updateBrowseId(browseId)
    }

    val uiState by viewModel.uiState.collectAsState()

    val mappedSongs = remember(uiState.listTrack) {
        uiState.listTrack.mapIndexed { index, track ->
            val durationMillis = track.durationSeconds?.toLong()?.times(1000) ?: 0L
            SongUiModel(
                id = track.videoId,
                title = track.title,
                artist = track.artists?.joinToString { it.name } ?: "",
                durationText = track.duration ?: "",
                durationMillis = durationMillis,
                trackNumber = index + 1,
                sourceType = SourceType.YOUTUBE,
                audioUri = null,
                album = uiState.title,
                remoteId = track.videoId
            )
        }
    }

    val mappedAlbum = remember(uiState.title, uiState.artist.name, uiState.year) {
        AlbumUiModel(
            id = uiState.browseId,
            title = uiState.title,
            artist = uiState.artist.name,
            sourceType = SourceType.YOUTUBE,
            releaseYear = uiState.year.toIntOrNull()
        )
    }

    AlbumDetailsScreen(
        album = mappedAlbum,
        albumSongs = mappedSongs,
        currentSongId = null, // TODO: Observe current playing song from SharedViewModel
        isLoading = uiState.loadState == LocalPlaylistState.PlaylistLoadState.Loading,
        errorMessage = if (uiState.loadState == LocalPlaylistState.PlaylistLoadState.Error) "Failed to load" else null,
        onPlaySongClick = { song, _ ->
            val track = uiState.listTrack.find { it.videoId == song.id }
            if (track != null) {
                viewModel.playTrack(track)
            }
        },
        onShuffleClick = { _ ->
            viewModel.shuffle()
        },
        onAddToPlaylistClick = { /* TODO */ },
        onRemoveFromLibraryClick = {
            viewModel.setAlbumLike() // Invert like status (acts as library sync for albums)
        },
        onDeleteClick = { /* TODO */ },
        onAddToLibraryClick = {
            viewModel.setAlbumLike()
        },
        onDownloadClick = {
            viewModel.downloadFullAlbum()
        }
    )
}
