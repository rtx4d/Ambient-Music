package com.sourajitk.ambient_music

import android.content.Intent
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log

class SleepQSTileService : TileService() {

  private val TAG = "SleepTileService"
  private val myGenre = "sleep"

  override fun onTileAdded() {
    super.onTileAdded()
    updateTileVisualsBasedOnServiceState()
  }

  override fun onStartListening() {
    super.onStartListening()
    Log.d(TAG, "Sleep Tile: Start")
    updateTileVisualsBasedOnServiceState()
  }

  override fun onStopListening() {
    super.onStopListening()
    Log.d(TAG, "Sleep Tile: Stop")
  }

  override fun onClick() {
    super.onClick()
    if (SongRepo.songs.isEmpty()) {
      Log.w(TAG, "No songs in parsed JSON")
      updateTileVisualsBasedOnServiceState(forceUnavailable = true)
      return
    }
    val isPlaying = MusicPlaybackService.isServiceCurrentlyPlaying
    val activeGenre = MusicPlaybackService.currentPlaylistGenre
    val isMyGenreActiveNow = isPlaying && myGenre.equals(activeGenre, ignoreCase = true)

    if (isMyGenreActiveNow) {
      applyVisuals(isMyGenreActive = false, forceUnavailable = false)
      val intent =
        Intent(this, MusicPlaybackService::class.java).apply {
          action = MusicPlaybackService.ACTION_TOGGLE_PLAYBACK_QS
        }
      startForegroundService(intent)
    } else {
      applyVisuals(isMyGenreActive = true, forceUnavailable = false)
      val intent =
        Intent(this, MusicPlaybackService::class.java).apply {
          action = "com.sourajitk.ambient_music.ACTION_PLAY_GENRE_SLEEP"
        }
      startForegroundService(intent)
    }
  }

  /** Fetches state from MusicPlaybackService and updates tile visuals. */
  private fun updateTileVisualsBasedOnServiceState(forceUnavailable: Boolean = false) {
    val isPlaying = MusicPlaybackService.isServiceCurrentlyPlaying
    val activeGenre = MusicPlaybackService.currentPlaylistGenre
    val isMyGenreActive = isPlaying && myGenre.equals(activeGenre, ignoreCase = true)
    applyVisuals(isMyGenreActive, forceUnavailable)
  }

  /** Applies the visual changes to the tile based on the given playback state of THIS genre. */
  private fun applyVisuals(isMyGenreActive: Boolean, forceUnavailable: Boolean) {
    val tile = qsTile ?: return
    tile.label = getString(R.string.tile_label_sleep)
    if (forceUnavailable || SongRepo.songs.isEmpty()) {
      tile.subtitle = getString(R.string.qs_subtitle_no_songs)
      tile.state = Tile.STATE_UNAVAILABLE
      tile.icon = Icon.createWithResource(this, R.drawable.ic_music_unavailable)
    } else {
      if (isMyGenreActive) {
        tile.subtitle = getString(R.string.qs_subtitle_playing)
        tile.state = Tile.STATE_ACTIVE
        tile.icon = Icon.createWithResource(this, R.drawable.ic_pause)
      } else {
        tile.subtitle = "Tap to play"
        tile.state = Tile.STATE_INACTIVE
        tile.icon = Icon.createWithResource(this, R.drawable.ic_play_arrow)
      }
    }
    tile.updateTile()
  }

  override fun onTileRemoved() {
    super.onTileRemoved()
    Log.d(TAG, "onTileRemoved")
  }
}
