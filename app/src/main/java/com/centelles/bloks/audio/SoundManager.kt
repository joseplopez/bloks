package com.centelles.bloks.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.centelles.bloks.R
import com.centelles.bloks.data.GameRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: GameRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private var mediaPlayer: MediaPlayer? = null
    private var soundPool: SoundPool? = null
    
    // IDs de sonidos cargados
    private var soundPlaceId: Int = 0
    private var soundClearId: Int = 0
    private var soundComboId: Int = 0
    private var soundGameOverId: Int = 0
    
    private var isMusicEnabled = true
    private var isSoundEnabled = true
    private var isAppInForeground = true

    init {
        initSoundPool()
        initMediaPlayer()
        observePreferences()
    }

    private fun initSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
            
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        try {
            soundPlaceId = soundPool?.load(context, R.raw.place_piece, 1) ?: 0
            soundClearId = soundPool?.load(context, R.raw.clear_line, 1) ?: 0
            soundComboId = soundPool?.load(context, R.raw.combo, 1) ?: 0
            soundGameOverId = soundPool?.load(context, R.raw.game_over, 1) ?: 0
            
            android.util.Log.d("SoundManager", "SoundPool initialized. IDs: place=$soundPlaceId, clear=$soundClearId, combo=$soundComboId, gameOver=$soundGameOverId")
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error loading sounds", e)
        }
    }

    private fun initMediaPlayer() {
        try {
            mediaPlayer = MediaPlayer.create(context, R.raw.block_bloom_theme_5min).apply {
                isLooping = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun observePreferences() {
        scope.launch {
            repository.musicEnabledFlow.distinctUntilChanged().collect { enabled ->
                android.util.Log.d("SoundManager", "Music preference changed: $enabled")
                isMusicEnabled = enabled
                updateMusicState()
            }
        }
        scope.launch {
            repository.soundEnabledFlow.distinctUntilChanged().collect { enabled ->
                android.util.Log.d("SoundManager", "Sound preference changed: $enabled")
                isSoundEnabled = enabled
            }
        }
    }

    private fun updateMusicState() {
        if (isMusicEnabled && isAppInForeground) {
            if (mediaPlayer == null) {
                initMediaPlayer()
            }
            if (mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
            }
        } else {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        }
    }

    fun playPlacePiece() {
        android.util.Log.d("SoundManager", "playPlacePiece() - enabled=$isSoundEnabled, id=$soundPlaceId")
        if (isSoundEnabled && soundPlaceId != 0) {
            val res = soundPool?.play(soundPlaceId, 1.0f, 1.0f, 1, 0, 1.0f)
            android.util.Log.d("SoundManager", "playPlacePiece() play result=$res")
        }
    }

    fun playClearLine() {
        android.util.Log.d("SoundManager", "playClearLine() - enabled=$isSoundEnabled, id=$soundClearId")
        if (isSoundEnabled && soundClearId != 0) {
            val res = soundPool?.play(soundClearId, 1.0f, 1.0f, 1, 0, 1.0f)
            android.util.Log.d("SoundManager", "playClearLine() play result=$res")
        }
    }

    fun playCombo() {
        android.util.Log.d("SoundManager", "playCombo() - enabled=$isSoundEnabled, id=$soundComboId")
        if (isSoundEnabled && soundComboId != 0) {
            val res = soundPool?.play(soundComboId, 1.0f, 1.0f, 1, 0, 1.0f)
            android.util.Log.d("SoundManager", "playCombo() play result=$res")
        }
    }

    fun playGameOver() {
        android.util.Log.d("SoundManager", "playGameOver() - enabled=$isSoundEnabled, id=$soundGameOverId")
        if (isSoundEnabled && soundGameOverId != 0) {
            val res = soundPool?.play(soundGameOverId, 1.0f, 1.0f, 1, 0, 1.0f)
            android.util.Log.d("SoundManager", "playGameOver() play result=$res")
        }
    }

    fun onActivityResumed() {
        isAppInForeground = true
        updateMusicState()
    }

    fun onActivityPaused() {
        isAppInForeground = false
        updateMusicState()
    }

    fun release() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        soundPool?.release()
        soundPool = null
    }
}
