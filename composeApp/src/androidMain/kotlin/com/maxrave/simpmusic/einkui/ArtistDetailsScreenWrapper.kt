package com.maxrave.simpmusic.einkui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.maxrave.simpmusic.viewModel.ArtistScreenState
import com.maxrave.simpmusic.viewModel.ArtistViewModel

@Composable
fun ArtistDetailsScreenWrapper(
    channelId: String,
    viewModel: ArtistViewModel,
    navController: NavController
) {
    LaunchedEffect(channelId) {
        viewModel.browseArtist(channelId)
    }

    val screenState by viewModel.artistScreenState.collectAsState()

    val mappedSongs = remember(screenState) {
        when (val state = screenState) {
            is ArtistScreenState.Success -> {
                state.data.popularSongs.mapIndexed { index, track ->
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
            else -> emptyList()
        }
    }

    val mappedAlbums = remember(screenState) {
        when (val state = screenState) {
            is ArtistScreenState.Success -> {
                state.data.albums?.results?.map { album ->
                    AlbumUiModel(
                        id = album.browseId,
                        title = album.title,
                        artist = state.data.title ?: "",
                        sourceType = SourceType.YOUTUBE,
                        releaseYear = album.year?.toIntOrNull()
                    )
                } ?: emptyList()
            }
            else -> emptyList()
        }
    }

    ArtistDetailsScreen(
        artistId = channelId,
        currentSongId = null,
        artistSongs = mappedSongs,
        artistAlbums = mappedAlbums,
        isLoading = screenState is ArtistScreenState.Loading,
        errorMessage = (screenState as? ArtistScreenState.Error)?.message,
        onPlaySongClick = { song, _ ->
            // In a real implementation this might use the SharedViewModel or the artist radio/shuffle endpoints.
            // For now, we can use the shuffle endpoint if we want, or handle individual tracks.
        },
        onAlbumClick = { album ->
            navController.navigate(Screen.AlbumDetails.createRoute(album.id))
        },
        onShuffleSongsClick = { _ ->
            when (val state = screenState) {
                is ArtistScreenState.Success -> {
                    state.data.shuffleParam?.let {
                        viewModel.onShuffleClick(it)
                    }
                }
                else -> {}
            }
        },
        onAddToPlaylistClick = { /* TODO */ },
        onRemoveFromLibraryClick = { /* TODO */ },
        onDeleteClick = { /* TODO */ },
        onAddToLibraryClick = { /* TODO */ },
        onDownloadClick = { /* TODO */ }
    )
}
