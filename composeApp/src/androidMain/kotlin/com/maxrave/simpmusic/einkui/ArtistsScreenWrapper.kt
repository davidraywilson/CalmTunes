package com.maxrave.simpmusic.einkui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.maxrave.simpmusic.viewModel.LibraryDynamicPlaylistViewModel

@Composable
fun ArtistsScreenWrapper(viewModel: LibraryDynamicPlaylistViewModel, navController: NavController) {
    val artistsState by viewModel.listFollowedArtist.collectAsState()

    val mappedArtists = remember(artistsState) {
        artistsState.map { artist ->
            ArtistUiModel(
                id = artist.channelId,
                name = artist.name,
                songCount = 0, // simpmusic ArtistEntity doesn't have songCount directly on it without joining
                albumCount = 0 
            )
        }
    }

    ArtistsScreen(
        artists = mappedArtists,
        isLoading = false,
        errorMessage = null,
        isSyncInProgress = false,
        hasAnySongs = true, // Not strictly checking library songs here for now
        onOpenStreamingSettingsClick = { /* TODO */ },
        onOpenLocalSettingsClick = { /* TODO */ },
        onArtistClick = { artist -> 
            navController.navigate(Screen.ArtistDetails.createRoute(artist.id))
        }
    )
}
