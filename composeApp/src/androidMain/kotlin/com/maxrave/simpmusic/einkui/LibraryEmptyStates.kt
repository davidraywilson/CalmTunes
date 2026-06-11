package com.maxrave.simpmusic.einkui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudita.mmd.components.text.TextMMD

@Composable
fun LibraryEmptyState(
    title: String,
    body: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextMMD(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextMMD(
            text = body,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun SongsEmptyState() {
    LibraryEmptyState(
        title = "No songs yet",
        body = "Search YouTube to find your favorite songs and start building your library."
    )
}

@Composable
fun PlaylistsEmptyState() {
    LibraryEmptyState(
        title = "No playlists",
        body = "You haven't created or liked any playlists yet."
    )
}

@Composable
fun ArtistsEmptyState() {
    LibraryEmptyState(
        title = "No artists",
        body = "Follow your favorite artists to see them here."
    )
}

@Composable
fun AlbumsEmptyState() {
    LibraryEmptyState(
        title = "No albums",
        body = "Like some albums to add them to your collection."
    )
}

@Composable
fun SearchEmptyState(query: String) {
    if (query.isBlank()) {
        LibraryEmptyState(
            title = "Search",
            body = "Search for songs, artists, and albums."
        )
    } else {
        LibraryEmptyState(
            title = "No results found",
            body = "We couldn't find anything matching \"$query\"."
        )
    }
}
