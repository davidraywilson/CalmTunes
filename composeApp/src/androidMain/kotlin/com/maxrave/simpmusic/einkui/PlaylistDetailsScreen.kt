package com.maxrave.simpmusic.einkui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.mudita.mmd.components.buttons.ButtonMMD
import com.mudita.mmd.components.buttons.FloatingActionButtonMMD
import com.mudita.mmd.components.checkbox.CheckboxMMD
import com.mudita.mmd.components.lazy.LazyColumnMMD
import com.mudita.mmd.components.text.TextMMD
import kotlinx.coroutines.launch

@Composable
fun PlaylistDetailsScreen(
    playlistId: String?,
    currentSongId: String?,
    playlistSongs: List<SongUiModel>,
    isLoading: Boolean,
    errorMessage: String?,
    onMoveSong: (fromIndex: Int, toIndex: Int) -> Unit,
    isInEditMode: Boolean,
    selectedSongIds: Set<String>,
    onSongSelectionChange: (songId: String, isSelected: Boolean) -> Unit,
    onPlaySongClick: (SongUiModel, List<SongUiModel>) -> Unit,
    onAddSongsClick: () -> Unit,
    onShuffleClick: (List<SongUiModel>) -> Unit,
    onAddToPlaylistClick: (SongUiModel) -> Unit,
    onRemoveFromLibraryClick: (SongUiModel) -> Unit,
    onDeleteClick: (SongUiModel) -> Unit,
    onAddToLibraryClick: (SongUiModel) -> Unit,
    onDownloadClick: (SongUiModel) -> Unit,
) {
    // Reorder Logic
    fun moveSong(fromIndex: Int, toIndex: Int) {
        onMoveSong(fromIndex, toIndex)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    TextMMD(text = "Loading playlist...")
                }
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    TextMMD(text = errorMessage!!)
                }
            }

            playlistSongs.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        TextMMD(
                            text = "No songs in this playlist",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ButtonMMD(
                            onClick = onAddSongsClick,
                        ) {
                            TextMMD(text = "Add songs")
                        }
                    }
                }
            }

            else -> {
                LazyColumnMMD(contentPadding = PaddingValues(16.dp)) {
                    items(playlistSongs.size) { index ->
                        val song = playlistSongs[index]
                        val isLast = song == playlistSongs.lastOrNull()
                        val isSelected = selectedSongIds.contains(song.id)

                        if (isInEditMode) {
                            EditablePlaylistSongItem(
                                song = song,
                                isSelected = isSelected,
                                onSelectionChange = { nowSelected ->
                                    onSongSelectionChange(song.id, nowSelected)
                                },
                                canMoveUp = index > 0,
                                canMoveDown = index < playlistSongs.lastIndex,
                                // Use local function
                                onMoveUp = { moveSong(index, index - 1) },
                                onMoveDown = { moveSong(index, index + 1) },
                                showDivider = !isLast,
                            )
                        } else {
                            SongItem(
                                song = song,
                                isCurrentlyPlaying = song.id == currentSongId,
                                onClick = { onPlaySongClick(song, playlistSongs) },
                                onAddToPlaylist = { onAddToPlaylistClick(song) },
                                onRemoveFromLibrary = { onRemoveFromLibraryClick(song) },
                                onDelete = { onDeleteClick(song) },
                                onAddToLibrary = { onAddToLibraryClick(song) },
                                onDownload = { onDownloadClick(song) },
                                isInLibrary = true,
                                canDownload = song.sourceType == SourceType.YOUTUBE,
                                showDivider = !isLast,
                            )
                        }
                    }
                }
            }
        }

        if (!isLoading && errorMessage == null && playlistSongs.isNotEmpty() && !isInEditMode) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End,
            ) {
                FloatingActionButtonMMD(
                    onClick = { onShuffleClick(playlistSongs) },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shuffle,
                        contentDescription = "Shuffle playlist",
                    )
                }

                FloatingActionButtonMMD(
                    onClick = onAddSongsClick,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add songs",
                    )
                }
            }
        }
    }
}

@Composable
private fun EditablePlaylistSongItem(
    song: SongUiModel,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    showDivider: Boolean,
) {
    val toggleSelection: () -> Unit = {
        onSelectionChange(!isSelected)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = toggleSelection)
            .padding(bottom = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CheckboxMMD(
                checked = isSelected,
                onCheckedChange = { checked ->
                    onSelectionChange(checked)
                },
                modifier = Modifier.padding(0.dp),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            ) {
                TextMMD(
                    text = song.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))

                val subtitle = buildString {
                    val baseArtist = song.artist
                    if (baseArtist.isNotBlank()) {
                        append(baseArtist)
                    }
                    val duration = song.durationText
                    if (!duration.isNullOrBlank()) {
                        if (isNotEmpty()) append(" • ")
                        append(duration)
                    }
                }

                if (subtitle.isNotEmpty()) {
                    TextMMD(
                        text = subtitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (canMoveUp) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowUpward,
                        contentDescription = "Move up",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(4.dp)
                            .clickable(onClick = onMoveUp),
                    )
                }
                if (canMoveDown) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowDownward,
                        contentDescription = "Move down",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(4.dp)
                            .clickable(onClick = onMoveDown),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (showDivider) {
            DashedDivider(thickness = 1.dp)
        }
    }
}