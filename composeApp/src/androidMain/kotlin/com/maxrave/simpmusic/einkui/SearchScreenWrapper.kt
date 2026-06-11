package com.maxrave.simpmusic.einkui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.viewModel.SearchViewModel
import com.maxrave.simpmusic.viewModel.SearchScreenUIState
import com.maxrave.simpmusic.viewModel.SharedViewModel
import kotlinx.coroutines.delay

import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenWrapper(viewModel: SearchViewModel, sharedViewModel: SharedViewModel) {
    val searchState by viewModel.searchScreenState.collectAsState()
    val searchUiState by viewModel.searchScreenUIState.collectAsState()
    
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(query) {
        if (query.isNotEmpty() && query.length >= 3) {
            delay(500) // debounce
            viewModel.searchAll(query)
        }
    }

    val mappedSongs = remember(searchState.searchSongsResult) {
        searchState.searchSongsResult.map { song ->
            SongUiModel(
                id = song.videoId,
                title = song.title ?: "",
                artist = song.artists?.joinToString { it.name } ?: "",
                durationText = song.duration ?: "",
                durationMillis = 0L, // Need to parse if required
                trackNumber = null,
                sourceType = SourceType.YOUTUBE,
                audioUri = null,
                album = song.album?.name,
                remoteId = song.videoId
            )
        }
    }

    val mappedAlbums = remember(searchState.searchAlbumsResult) {
        searchState.searchAlbumsResult.map { album ->
            AlbumUiModel(
                id = album.browseId,
                title = album.title,
                artist = album.artists?.joinToString { it.name } ?: "",
                sourceType = album.resultType ?: "album",
                releaseYear = album.year.toString().toIntOrNull()
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        com.mudita.mmd.components.search_bar.SearchBarDefaultsMMD.InputField(
            query = query,
            onQueryChange = { query = it },
            onSearch = {
                // optional: keyboardController?.hide()
            },
            expanded = true,
            onExpandedChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { com.mudita.mmd.components.text.TextMMD(text = "Search...") },
            trailingIcon = {
                androidx.compose.foundation.layout.Row {
                    if (query.isNotEmpty()) {
                        androidx.compose.material3.IconButton(
                            onClick = { query = "" },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = "Clear search",
                            )
                        }
                    }
                    androidx.compose.material3.IconButton(
                        onClick = {
                            // optional: keyboardController?.hide()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                        )
                    }
                }
            }
        )

        SearchScreen(
            query = query,
            isAuthenticated = true,
            isSearching = searchUiState is SearchScreenUIState.Loading,
            errorMessage = if (searchUiState is SearchScreenUIState.Error) "An error occurred" else null,
            songs = mappedSongs,
            albums = mappedAlbums,
            localSongs = emptyList(), // Local search can be implemented by injecting LibraryViewModel
            selectedTab = selectedTab,
            onSelectedTabChange = { selectedTab = it },
            onPlaySongClick = { song ->
                val videoId = song.remoteId ?: song.id
                sharedViewModel.loadSharedMediaItem(videoId)
            },
            onAlbumClick = { /* TODO: navigate to album details */ },
            onAddToPlaylistClick = { /* TODO */ },
            onRemoveFromLibraryClick = { /* TODO */ },
            onDeleteClick = { /* TODO */ },
            onAddToLibraryClick = { /* TODO */ },
            onDownloadClick = { /* TODO */ },
            librarySongIds = emptySet()
        )
    }
}
