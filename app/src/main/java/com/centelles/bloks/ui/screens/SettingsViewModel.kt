package com.centelles.bloks.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.centelles.bloks.data.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    val soundEnabled = repository.soundEnabledFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    val musicEnabled = repository.musicEnabledFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    val vibrationEnabled = repository.vibrationEnabledFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSoundEnabled(enabled)
        }
    }

    fun toggleMusic(enabled: Boolean) {
        viewModelScope.launch {
            repository.setMusicEnabled(enabled)
        }
    }

    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch {
            repository.setVibrationEnabled(enabled)
        }
    }
}
