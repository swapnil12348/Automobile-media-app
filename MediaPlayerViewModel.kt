package com.example.automobilemediaapp

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Track(
    val title: String,
    val artist: String,
    val albumArt: Bitmap
)

data class MediaPlayerState(
    val currentTrack: Track = Track("", "", Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)),
    val duration: Int = 0,
    val currentPosition: Int = 0,
    val isPlaying: Boolean = false
)

class MediaPlayerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MediaPlayerState())
    val uiState: StateFlow<MediaPlayerState> = _uiState.asStateFlow()

    fun onPlayPauseClick() {
        _uiState.value = _uiState.value.copy(
            isPlaying = !_uiState.value.isPlaying
        )
    }

    fun onPreviousClick() {
        // Implement previous track logic
    }

    fun onNextClick() {
        // Implement next track logic
    }

    fun onSeek(position: Int) {
        _uiState.value = _uiState.value.copy(
            currentPosition = position
        )
    }
}