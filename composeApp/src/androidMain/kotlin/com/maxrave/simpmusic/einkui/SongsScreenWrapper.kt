package com.maxrave.simpmusic.einkui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.maxrave.simpmusic.viewModel.LibraryDynamicPlaylistViewModel
import com.maxrave.simpmusic.viewModel.SharedViewModel
import com.maxrave.simpmusic.extension.formatDuration

@Composable
fun SongsScreenWrapper(viewModel: LibraryDynamicPlaylistViewModel) {
    val songsState by viewModel.listFavoriteSong.collectAsState()
    
    val mappedSongs = remember(songsState) {
        songsState.map { song ->
            val durationMillis = song.duration.toLong() * 1000
            SongUiModel(
                id = song.videoId,
                title = song.title,
                artist = song.artistName?.joinToString() ?: "",
                durationText = song.duration,
                durationMillis = song.durationSeconds.toLong() * 1000L,
                trackNumber = null,
                sourceType = SourceType.YOUTUBE,
                audioUri = null,
                album = song.albumName,
                remoteId = song.videoId
            )
        }
    }

    SongsScreen(
        songs = mappedSongs,
        isLoading = false,
        errorMessage = null,
        currentSongId = null,
        isSyncInProgress = false,
        onPlaySongClick = { song ->
            val videoId = song.remoteId ?: song.id
            viewModel.playSong(videoId, com.maxrave.simpmusic.ui.screen.library.LibraryDynamicPlaylistType.Favorite)
        },
        onShuffleClick = { 
            viewModel.shuffle(com.maxrave.simpmusic.ui.screen.library.LibraryDynamicPlaylistType.Favorite)
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
