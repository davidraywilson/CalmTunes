package com.maxrave.simpmusic.einkui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.simpmusic.viewModel.SettingsViewModel

@Composable
fun SettingsScreenWrapper(
    viewModel: SettingsViewModel,
    navController: NavController
) {
    LaunchedEffect(Unit) {
        viewModel.getData()
    }

    var selectedTab by remember { mutableStateOf(0) }
    
    // We default to YouTube for now, as that's what SimpMusic supports
    val streamingProvider by remember { mutableStateOf(StreamingProvider.YOUTUBE) }
    
    val completeAlbumsWithYouTube by remember { mutableStateOf(false) } // Stub for now
    
    val includeLocalMusic by remember { mutableStateOf(false) } // Stub for now
    val localFolders = remember { listOf<String>() } // Stub for now
    
    // Check if YouTube Music is authenticated (from loggedIn DataStore)
    val loggedInState by viewModel.loggedIn.collectAsState()
    val isYouTubeAuthenticated = loggedInState == DataStoreManager.TRUE

    SettingsScreen(
        selectedTab = selectedTab,
        onSelectedTabChange = { selectedTab = it },
        streamingProvider = streamingProvider,
        onStreamingProviderChange = { /* TODO */ },
        completeAlbumsWithYouTube = completeAlbumsWithYouTube,
        onCompleteAlbumsWithYouTubeChange = { /* TODO */ },
        includeLocalMusic = includeLocalMusic,
        localFolders = localFolders,
        isAppleMusicAuthenticated = false,
        isYouTubeAuthenticated = isYouTubeAuthenticated,
        hasBatteryOptimizationExemption = true, // Stub
        onConnectAppleMusicClick = { /* TODO */ },
        onConnectYouTubeClick = {
            navController.navigate(Screen.Login.route)
        },
        onRequestBatteryOptimizationExemption = { /* TODO */ },
        onIncludeLocalMusicChange = { /* TODO */ },
        onAddFolderClick = { /* TODO */ },
        onRemoveFolderClick = { /* TODO */ },
        onRescanLocalMusicClick = { /* TODO */ },
        isRescanningLocal = false,
        localScanProgress = 0f,
        isIngestingLocal = false,
        localIngestProgress = 0f,
        localScanTotalDiscovered = null,
        localScanSkippedUnchanged = null,
        localScanIndexedNewOrUpdated = null,
        localScanDeletedMissing = null
    )
}
