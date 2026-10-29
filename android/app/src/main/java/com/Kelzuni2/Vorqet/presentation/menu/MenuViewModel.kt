package com.Kelzuni2.Vorqet.presentation.menu

import androidx.lifecycle.ViewModel
import com.Kelzuni2.Vorqet.core.config.GameConfig
import com.Kelzuni2.Vorqet.domain.model.DailyGoal
import com.Kelzuni2.Vorqet.domain.model.GardenSettings
import com.Kelzuni2.Vorqet.domain.repository.SessionRepository
import com.Kelzuni2.Vorqet.domain.repository.SettingsRepository
import com.Kelzuni2.Vorqet.domain.usecase.GetDailyProgressUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MenuViewModel(
    private val dailyProgress: GetDailyProgressUseCase,
    private val sessionRepository: SessionRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _goal = MutableStateFlow(
        DailyGoal(0, GameConfig.DEFAULT_DAILY_GOAL, 0)
    )
    val goal: StateFlow<DailyGoal> = _goal.asStateFlow()

    private val _bestScore = MutableStateFlow(0)
    val bestScore: StateFlow<Int> = _bestScore.asStateFlow()

    private val _totalBlooms = MutableStateFlow(0)
    val totalBlooms: StateFlow<Int> = _totalBlooms.asStateFlow()

    private val _settings = MutableStateFlow(
        GardenSettings(
            targetSum = GameConfig.DEFAULT_TARGET,
            sessionSeconds = GameConfig.DEFAULT_SESSION_SECONDS,
            dailyGoal = GameConfig.DEFAULT_DAILY_GOAL,
            reducedMotion = false,
            gardenerName = ""
        )
    )
    val settings: StateFlow<GardenSettings> = _settings.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val loaded = settingsRepository.load()
        _settings.value = loaded
        _goal.value = dailyProgress(loaded.dailyGoal)
        _bestScore.value = sessionRepository.bestScore()
        _totalBlooms.value = sessionRepository.totalBlooms()
    }
}
