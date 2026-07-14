package com.maxrave.simpmusic.einkui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.maxrave.simpmusic.viewModel.LibraryDynamicPlaylistViewModel

@Composable
fun SongsScreenWrapper(viewModel: LibraryDynamicPlaylistViewModel) {
    val songsState by viewModel.listLibrarySongs.collectAsState()
    val isLoading by viewModel.librarySongsLoading.collectAsState()
    val currentSongId by viewModel.nowPlayingVideoId.collectAsState()

    val mappedSongs = remember(songsState) {
        songsState.mapIndexed { index, track ->
            SongUiModel(
                id = track.videoId,
                title = track.title,
                artist = track.artistName?.joinToString() ?: "",
                durationText = track.duration,
                durationMillis = track.durationSeconds.toLong().times(1000),
                trackNumber = index + 1,
                sourceType = SourceType.YOUTUBE,
                audioUri = null,
                album = track.albumName,
                remoteId = track.videoId,
            )
        }
    }

    SongsScreen(
        songs = mappedSongs,
        isLoading = isLoading,
        errorMessage = null,
        currentSongId = currentSongId.takeIf { it.isNotEmpty() },
        isSyncInProgress = false,
        onPlaySongClick = { song ->
            viewModel.playSongFromLibrary(song.id)
        },
        onShuffleClick = {
            viewModel.shuffleLibrary()
        },
        onAddToPlaylistClick = { /* TODO */ },
        onRemoveFromLibraryClick = { /* TODO */ },
        onDeleteClick = { /* TODO */ },
        onAddToLibraryClick = { /* TODO */ },
        onDownloadClick = { /* TODO */ },
        onOpenStreamingSettingsClick = { /* TODO */ },
        onOpenLocalSettingsClick = { /* TODO */ },
    )
}
