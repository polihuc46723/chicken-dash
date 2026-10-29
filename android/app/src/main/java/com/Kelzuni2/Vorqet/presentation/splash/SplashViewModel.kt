package com.Kelzuni2.Vorqet.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelzuni2.Vorqet.core.config.GameConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class SplashViewModel : ViewModel() {

    private val _handOffReady = MutableStateFlow(false)
    val handOffReady: StateFlow<Boolean> = _handOffReady.asStateFlow()

    private var timerStarted = false
    private var handedOff = false

    init {
        startCountdown()
    }

    private fun startCountdown() {
        if (timerStarted) {
            return
        }
        timerStarted = true
        viewModelScope.launch {
            var elapsed = 0L
            while (elapsed < GameConfig.LOADER_DURATION_MS) {
                delay(STEP_MS)
                elapsed += STEP_MS
            }
            if (!handedOff) {
                _handOffReady.value = true
            }
        }
    }

    fun consumeHandOff() {
        handedOff = true
        _handOffReady.value = false
    }

    companion object {
        private const val STEP_MS = 200L
    }
}
