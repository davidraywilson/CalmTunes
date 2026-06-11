package com.maxrave.simpmusic.einkui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.maxrave.domain.data.entities.PlaylistEntity
import com.maxrave.domain.utils.LocalResource
import com.maxrave.simpmusic.viewModel.LibraryViewModel

@Composable
fun PlaylistsScreenWrapper(viewModel: LibraryViewModel, navController: NavController) {
    val favoritePlaylistsState by viewModel.favoritePlaylist.collectAsState()
    val localPlaylistsState by viewModel.yourLocalPlaylist.collectAsState()
    val youTubePlaylistsState by viewModel.youTubePlaylist.collectAsState()
    val youTubeMixesState by viewModel.youTubeMixForYou.collectAsState()

    LaunchedEffect(Unit) {
        if (youTubePlaylistsState.data.isNullOrEmpty()) {
            viewModel.getYouTubePlaylist()
        }
        if (youTubeMixesState.data.isNullOrEmpty()) {
            viewModel.getYouTubeMixedForYou()
        }
    }

    val mappedPlaylists = remember(favoritePlaylistsState, localPlaylistsState, youTubePlaylistsState, youTubeMixesState) {
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

        val mappedYouTube = youTubePlaylistsState.data?.map {
            PlaylistUiModel(
                id = it.browseId,
                name = it.title,
                description = it.author,
                songCount = it.itemCount.toIntOrNull()
            )
        } ?: emptyList()

        val mappedMixes = youTubeMixesState.data?.map {
            PlaylistUiModel(
                id = it.browseId,
                name = it.title,
                description = "YouTube Mix",
                songCount = it.itemCount.toIntOrNull()
            )
        } ?: emptyList()

        (mappedFavs + mappedLocals + mappedYouTube + mappedMixes).distinctBy { it.id }
    }

    val isLoading = youTubePlaylistsState is LocalResource.Loading || youTubeMixesState is LocalResource.Loading

    PlaylistsScreen(
        playlists = mappedPlaylists,
        isInEditMode = false,
        isLoading = isLoading,
        onPlaylistClick = { playlist -> 
            navController.navigate(Screen.PlaylistDetails.createRoute(playlist.id))
        },
        onAddPlaylistClick = { /* TODO */ },
        onSelectionChanged = { /* TODO */ }
    )
}
