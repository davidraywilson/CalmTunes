package com.maxrave.simpmusic.viewModel

import androidx.lifecycle.viewModelScope
import com.maxrave.common.Config
import com.maxrave.common.Config.REMOVED_SONG_DATE_TIME
import com.maxrave.domain.data.entities.ArtistEntity
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.mediaservice.handler.PlaylistType
import com.maxrave.domain.mediaservice.handler.QueueData
import com.maxrave.domain.repository.ArtistRepository
import com.maxrave.domain.repository.PlaylistRepository
import com.maxrave.domain.repository.SongRepository
import com.maxrave.domain.utils.Resource
import com.maxrave.domain.utils.toArrayListTrack
import com.maxrave.domain.utils.toTrack
import com.maxrave.simpmusic.ui.screen.library.LibraryDynamicPlaylistType
import com.maxrave.simpmusic.viewModel.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.playlist

class LibraryDynamicPlaylistViewModel(
    private val songRepository: SongRepository,
    private val artistRepository: ArtistRepository,
    private val playlistRepository: PlaylistRepository,
) : BaseViewModel() {
    private val _listFavoriteSong: MutableStateFlow<List<SongEntity>> = MutableStateFlow(emptyList())
    val listFavoriteSong: StateFlow<List<SongEntity>> get() = _listFavoriteSong

    /** Songs saved to the user's YouTube Music library (fetched from the LM playlist). */
    private val _listLibrarySongs: MutableStateFlow<List<SongEntity>> = MutableStateFlow(emptyList())
    val listLibrarySongs: StateFlow<List<SongEntity>> get() = _listLibrarySongs

    private val _librarySongsLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val librarySongsLoading: StateFlow<Boolean> get() = _librarySongsLoading

    private val _listFollowedArtist: MutableStateFlow<List<ArtistEntity>> = MutableStateFlow(emptyList())
    val listFollowedArtist: StateFlow<List<ArtistEntity>> get() = _listFollowedArtist

    private val _listMostPlayedSong: MutableStateFlow<List<SongEntity>> = MutableStateFlow(emptyList())
    val listMostPlayedSong: StateFlow<List<SongEntity>> get() = _listMostPlayedSong

    private val _listDownloadedSong: MutableStateFlow<List<SongEntity>> = MutableStateFlow(emptyList())
    val listDownloadedSong: StateFlow<List<SongEntity>> get() = _listDownloadedSong

    init {
        getFavoriteSong()
        fetchLibrarySongs()
        getFollowedArtist()
        getMostPlayedSong()
        getDownloadedSong()
    }

    private fun getFavoriteSong() {
        viewModelScope.launch {
            songRepository.getLikedSongs().collectLatest { likedSong ->
                _listFavoriteSong.value =
                    likedSong.sortedByDescending {
                        it.favoriteAt ?: REMOVED_SONG_DATE_TIME
                    }
            }
        }
    }

    /** Fetches songs saved to the user's YouTube Music library via the local database. */
    fun fetchLibrarySongs() {
        viewModelScope.launch {
            _librarySongsLoading.value = true
            songRepository.getLibrarySongs().collectLatest { librarySongs ->
                _listLibrarySongs.value = librarySongs
                _librarySongsLoading.value = false
            }
        }
    }

    private fun getFollowedArtist() {
        viewModelScope.launch {
            artistRepository.getFollowedArtists().collectLatest { followedArtist ->
                _listFollowedArtist.value =
                    followedArtist.sortedByDescending {
                        it.followedAt ?: REMOVED_SONG_DATE_TIME
                    }
            }
        }
    }

    private fun getMostPlayedSong() {
        viewModelScope.launch {
            songRepository.getMostPlayedSongs().collectLatest { mostPlayedSong ->
                _listMostPlayedSong.value = mostPlayedSong.sortedByDescending { it.totalPlayTime }
            }
        }
    }

    private fun getDownloadedSong() {
        viewModelScope.launch {
            songRepository.getDownloadedSongs().collectLatest { downloadedSong ->
                _listDownloadedSong.value =
                    (downloadedSong ?: emptyList()).sortedByDescending {
                        it.downloadedAt ?: REMOVED_SONG_DATE_TIME
                    }
            }
        }
    }

    fun playSong(
        videoId: String,
        type: LibraryDynamicPlaylistType,
    ) {
        val (targetList, playTrack) =
            when (type) {
                LibraryDynamicPlaylistType.Favorite -> listFavoriteSong.value to listFavoriteSong.value.find { it.videoId == videoId }
                LibraryDynamicPlaylistType.Downloaded -> listDownloadedSong.value to listDownloadedSong.value.find { it.videoId == videoId }
                LibraryDynamicPlaylistType.Followed -> return
                LibraryDynamicPlaylistType.MostPlayed -> listMostPlayedSong.value to listMostPlayedSong.value.find { it.videoId == videoId }
                else -> return
            }
        if (playTrack == null) return
        setQueueData(
            QueueData.Data(
                listTracks = targetList.toArrayListTrack(),
                firstPlayedTrack = playTrack.toTrack(),
                playlistId = null,
                playlistName = "${
                    getString(
                        Res.string.playlist,
                    )
                } ${getString(type.name())}",
                playlistType = PlaylistType.RADIO,
                continuation = null,
            ),
        )
        loadMediaItem(
            playTrack.toTrack(),
            Config.PLAYLIST_CLICK,
            targetList.indexOf(playTrack).coerceAtLeast(0),
        )
    }

    private fun getSongList(type: LibraryDynamicPlaylistType): List<SongEntity> =
        when (type) {
            LibraryDynamicPlaylistType.Favorite -> listFavoriteSong.value
            LibraryDynamicPlaylistType.Downloaded -> listDownloadedSong.value
            LibraryDynamicPlaylistType.MostPlayed -> listMostPlayedSong.value
            else -> emptyList()
        }

    fun playAll(type: LibraryDynamicPlaylistType) {
        val targetList = getSongList(type)
        val firstTrack = targetList.firstOrNull() ?: return
        setQueueData(
            QueueData.Data(
                listTracks = targetList.toArrayListTrack(),
                firstPlayedTrack = firstTrack.toTrack(),
                playlistId = null,
                playlistName = "${getString(Res.string.playlist)} ${getString(type.name())}",
                playlistType = PlaylistType.RADIO,
                continuation = null,
            ),
        )
        loadMediaItem(
            firstTrack.toTrack(),
            Config.PLAYLIST_CLICK,
            0,
        )
    }

    fun shuffle(type: LibraryDynamicPlaylistType) {
        val targetList = getSongList(type)
        if (targetList.isEmpty()) return
        val shuffledList = targetList.shuffled()
        val firstTrack = shuffledList.first()
        setQueueData(
            QueueData.Data(
                listTracks = shuffledList.toArrayListTrack(),
                firstPlayedTrack = firstTrack.toTrack(),
                playlistId = null,
                playlistName = "${getString(Res.string.playlist)} ${getString(type.name())}",
                playlistType = PlaylistType.RADIO,
                continuation = null,
            ),
        )
        loadMediaItem(
            firstTrack.toTrack(),
            Config.PLAYLIST_CLICK,
            0,
        )
    }

    fun playSongFromLibrary(videoId: String) {
        val targetList = listLibrarySongs.value
        val playTrack = targetList.find { it.videoId == videoId } ?: return
        setQueueData(
            QueueData.Data(
                listTracks = targetList.map { it.toTrack() } as ArrayList<Track>,
                firstPlayedTrack = playTrack.toTrack(),
                playlistId = "LM",
                playlistName = "${getString(Res.string.playlist)} Songs",
                playlistType = PlaylistType.RADIO,
                continuation = null,
            ),
        )
        loadMediaItem(
            playTrack.toTrack(),
            Config.PLAYLIST_CLICK,
            targetList.indexOf(playTrack).coerceAtLeast(0),
        )
    }

    fun shuffleLibrary() {
        val targetList = listLibrarySongs.value
        if (targetList.isEmpty()) return
        val shuffledList = targetList.shuffled()
        val firstTrack = shuffledList.first()
        setQueueData(
            QueueData.Data(
                listTracks = shuffledList.map { it.toTrack() } as ArrayList<Track>,
                firstPlayedTrack = firstTrack.toTrack(),
                playlistId = "LM",
                playlistName = "${getString(Res.string.playlist)} Songs",
                playlistType = PlaylistType.RADIO,
                continuation = null,
            ),
        )
        loadMediaItem(
            firstTrack.toTrack(),
            Config.PLAYLIST_CLICK,
            0,
        )
    }
}
