package com.maxrave.simpmusic.einkui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.maxrave.simpmusic.viewModel.PlaylistUIEvent
import com.maxrave.simpmusic.viewModel.PlaylistUIState
import com.maxrave.simpmusic.viewModel.PlaylistViewModel

@Composable
fun PlaylistDetailsScreenWrapper(
    playlistId: String,
    viewModel: PlaylistViewModel,
    navController: NavController
) {
    LaunchedEffect(playlistId) {
        viewModel.getData(playlistId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val tracks by viewModel.tracks.collectAsState()

    val mappedSongs = remember(tracks) {
        tracks.mapIndexed { index, track ->
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
                album = track.album?.name,
                remoteId = track.videoId
            )
        }
    }

    var selectedSongIds by remember { mutableStateOf(emptySet<String>()) }

    PlaylistDetailsScreen(
        playlistId = playlistId,
        currentSongId = null, // TODO: Observe current playing song from SharedViewModel
        playlistSongs = mappedSongs,
        isLoading = uiState is PlaylistUIState.Loading,
        errorMessage = (uiState as? PlaylistUIState.Error)?.message,
        onMoveSong = { fromIndex, toIndex ->
            // Reordering logic here, if supported by the Youtube music library backend
        },
        isInEditMode = false,
        selectedSongIds = selectedSongIds,
        onSongSelectionChange = { songId, isSelected ->
            selectedSongIds = if (isSelected) {
                selectedSongIds + songId
            } else {
                selectedSongIds - songId
            }
        },
        onPlaySongClick = { song, _ ->
            viewModel.onUIEvent(PlaylistUIEvent.ItemClick(song.id))
        },
        onAddSongsClick = { /* TODO */ },
        onShuffleClick = { _ ->
            viewModel.onUIEvent(PlaylistUIEvent.Shuffle)
        },
        onAddToPlaylistClick = { /* TODO */ },
        onRemoveFromLibraryClick = {
            // Wait, PlaylistViewModel handles library changes for the whole playlist usually via Favorite.
        },
        onDeleteClick = { /* TODO */ },
        onAddToLibraryClick = { /* TODO */ },
        onDownloadClick = { /* TODO */ }
    )
}
