package com.maxrave.simpmusic.einkui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.maxrave.domain.mediaservice.handler.RepeatState
import com.maxrave.simpmusic.viewModel.AlbumViewModel
import com.maxrave.simpmusic.viewModel.ArtistViewModel
import com.maxrave.simpmusic.viewModel.LibraryDynamicPlaylistViewModel
import com.maxrave.simpmusic.viewModel.LibraryViewModel
import com.maxrave.simpmusic.viewModel.PlaylistViewModel
import com.maxrave.simpmusic.viewModel.SearchViewModel
import com.maxrave.simpmusic.viewModel.SettingsViewModel
import com.maxrave.simpmusic.ui.screen.login.LoginScreen
import com.maxrave.simpmusic.viewModel.SharedViewModel
import com.maxrave.simpmusic.viewModel.UIEvent
import com.mudita.mmd.components.buttons.ButtonMMD
import com.mudita.mmd.components.nav_bar.NavigationBarItemMMD
import com.mudita.mmd.components.nav_bar.NavigationBarMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EinkApp(viewModel: SharedViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Now Playing state
    val nowPlayingState by viewModel.nowPlayingState.collectAsState()
    val nowPlayingScreenData by viewModel.nowPlayingScreenData.collectAsState()
    val controllerState by viewModel.controllerState.collectAsState()
    val timeline by viewModel.timeline.collectAsState()

    val hasNowPlaying = nowPlayingState?.isNotEmpty() == true
    var showNowPlaying by remember { mutableStateOf(false) }

    // Auto-open NowPlaying when a new track starts (mirrors CalmMusic behaviour)
    LaunchedEffect(nowPlayingState?.mediaItem?.mediaId) {
        if (hasNowPlaying) {
            showNowPlaying = true
        }
    }

    // Hide overlay if playback fully stops
    if (!hasNowPlaying) {
        showNowPlaying = false
    }

    com.mudita.mmd.ThemeMMD {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    val title = when (currentDestination?.route) {
                        Screen.Playlists.route -> "Playlists"
                        Screen.Artists.route -> "Artists"
                        Screen.Songs.route -> "Songs"
                        Screen.Albums.route -> "Albums"
                        Screen.More.route -> "More"
                        Screen.Search.route -> "Search"
                        else -> ""
                    }

                    if (title.isNotEmpty() && currentDestination?.route != Screen.Search.route) {
                        TopAppBarMMD(
                            title = {
                                TextMMD(
                                    text = title,
                                    fontWeight = FontWeight.Bold,
                                )
                            },
                            actions = {
                                // "Now Playing" button — matches CalmMusic pattern
                                if (hasNowPlaying) {
                                    ButtonMMD(
                                        onClick = { showNowPlaying = true },
                                        contentPadding = PaddingValues(8.dp),
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                    ) {
                                        TextMMD(
                                            text = "Now Playing",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }

                                IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = "Search",
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.White,
                                scrolledContainerColor = Color.White,
                                navigationIconContentColor = Color.Black,
                                titleContentColor = Color.Black,
                                actionIconContentColor = Color.Black,
                            ),
                            showDivider = true
                        )
                    }
                },
                bottomBar = {
                    val navRoutes = navItems.map { it.route }
                    if (currentDestination?.route in navRoutes) {
                        NavigationBarMMD(modifier = Modifier.padding(bottom = 2.dp)) {
                            navItems.forEach { screen ->
                                val isSelected =
                                    currentDestination?.hierarchy?.any { it.route == screen.route } == true
                                NavigationBarItemMMD(
                                    icon = {
                                        Icon(
                                            painter = rememberVectorPainter(image = screen.icon),
                                            contentDescription = screen.label,
                                        )
                                    },
                                    label = {
                                        TextMMD(
                                            text = screen.label,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                        )
                                    },
                                    selected = isSelected,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Screen.Songs.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Screen.Playlists.route) {
                        val libraryViewModel: LibraryViewModel = koinViewModel()
                        PlaylistsScreenWrapper(libraryViewModel, navController)
                    }
                    composable(Screen.Artists.route) {
                        val dynamicViewModel: LibraryDynamicPlaylistViewModel = koinViewModel()
                        ArtistsScreenWrapper(dynamicViewModel, navController)
                    }
                    composable(Screen.Songs.route) {
                        val dynamicViewModel: LibraryDynamicPlaylistViewModel = koinViewModel()
                        SongsScreenWrapper(dynamicViewModel)
                    }
                    composable(Screen.Albums.route) {
                        val libraryViewModel: LibraryViewModel = koinViewModel()
                        AlbumsScreenWrapper(libraryViewModel, navController)
                    }
                    composable(Screen.More.route) {
                        val settingsViewModel: SettingsViewModel = koinViewModel()
                        SettingsScreenWrapper(settingsViewModel, navController)
                    }
                    composable(Screen.Settings.route) {
                        val settingsViewModel: SettingsViewModel = koinViewModel()
                        SettingsScreenWrapper(settingsViewModel, navController)
                    }
                    composable(Screen.Login.route) {
                        LoginScreen(
                            innerPadding = innerPadding,
                            navController = navController,
                            hideBottomNavigation = { },
                            showBottomNavigation = { }
                        )
                    }
                    composable(Screen.Search.route) {
                        val searchViewModel: SearchViewModel = koinViewModel()
                        SearchScreenWrapper(searchViewModel, viewModel)
                    }
                    composable(Screen.AlbumDetails.route + "/{browseId}") { navBackStackEntry ->
                        val browseId = navBackStackEntry.arguments?.getString("browseId") ?: ""
                        val albumViewModel: AlbumViewModel = koinViewModel()
                        AlbumDetailsScreenWrapper(
                            browseId = browseId,
                            viewModel = albumViewModel,
                            navController = navController
                        )
                    }
                    composable(Screen.ArtistDetails.route + "/{channelId}") { navBackStackEntry ->
                        val channelId = navBackStackEntry.arguments?.getString("channelId") ?: ""
                        val artistViewModel: ArtistViewModel = koinViewModel()
                        ArtistDetailsScreenWrapper(
                            channelId = channelId,
                            viewModel = artistViewModel,
                            navController = navController
                        )
                    }
                    composable(Screen.PlaylistDetails.route + "/{playlistId}") { navBackStackEntry ->
                        val playlistId = navBackStackEntry.arguments?.getString("playlistId") ?: ""
                        val playlistViewModel: PlaylistViewModel = koinViewModel()
                        PlaylistDetailsScreenWrapper(
                            playlistId = playlistId,
                            viewModel = playlistViewModel,
                            navController = navController
                        )
                    }
                }
            }

            // Full-screen Now Playing overlay — same pattern as CalmMusic
            if (showNowPlaying && hasNowPlaying) {
                BackHandler { showNowPlaying = false }

                val repeatMode = when (controllerState.repeatState) {
                    RepeatState.None -> RepeatMode.OFF
                    RepeatState.All -> RepeatMode.QUEUE
                    RepeatState.One -> RepeatMode.ONE
                }

                NowPlayingScreen(
                    title = nowPlayingScreenData.nowPlayingTitle,
                    artist = nowPlayingScreenData.artistName,
                    album = null,
                    isPlaying = controllerState.isPlaying,
                    isLoading = timeline.loading,
                    currentPosition = timeline.current.coerceAtLeast(0L),
                    duration = timeline.total.coerceAtLeast(0L),
                    repeatMode = repeatMode,
                    isShuffleOn = controllerState.isShuffle,
                    onPlayPauseClick = { viewModel.onUIEvent(UIEvent.PlayPause) },
                    onSeek = { newPositionMs ->
                        if (timeline.total > 0L) {
                            val progress = newPositionMs.toFloat() / timeline.total.toFloat()
                            viewModel.onUIEvent(UIEvent.UpdateProgress(progress))
                        }
                    },
                    onSeekBackwardClick = { viewModel.onUIEvent(UIEvent.Previous) },
                    onSeekForwardClick = { viewModel.onUIEvent(UIEvent.Next) },
                    onShuffleClick = { viewModel.onUIEvent(UIEvent.Shuffle) },
                    onRepeatClick = { viewModel.onUIEvent(UIEvent.Repeat) },
                    onAddToPlaylistClick = { /* TODO */ },
                    onBackClick = { showNowPlaying = false },
                    isVideo = nowPlayingScreenData.isVideo,
                    player = null,
                    canDownload = false,
                    isDownloadInProgress = false,
                    onDownloadClick = { /* TODO */ },
                    onCancelDownloadClick = { /* TODO */ },
                    canAddToLibrary = true,
                    onAddToLibraryClick = { viewModel.onUIEvent(UIEvent.ToggleLike) },
                    isInLibrary = viewModel.likeStatus.collectAsState().value,
                    sourceType = SourceType.YOUTUBE,
                )
            }
        }
    }
}
