package com.Kelzuni2.Vorqet.presentation.gameover

import androidx.lifecycle.ViewModel
import com.Kelzuni2.Vorqet.core.config.GameConfig
import com.Kelzuni2.Vorqet.domain.model.DailyGoal
import com.Kelzuni2.Vorqet.domain.model.GardenSettings
import com.Kelzuni2.Vorqet.domain.repository.SettingsRepository
import com.Kelzuni2.Vorqet.domain.usecase.GetDailyProgressUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameOverViewModel(
    private val dailyProgress: GetDailyProgressUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _goal = MutableStateFlow(
        DailyGoal(0, GameConfig.DEFAULT_DAILY_GOAL, 0)
    )
    val goal: StateFlow<DailyGoal> = _goal.asStateFlow()

    private val _settings = MutableStateFlow(fallbackSettings())
    val settings: StateFlow<GardenSettings> = _settings.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val loaded = try {
            settingsRepository.load()
        } catch (e: Exception) {
            fallbackSettings()
        }
        _settings.value = loaded
        _goal.value = try {
            dailyProgress(loaded.dailyGoal)
        } catch (e: Exception) {
            DailyGoal(0, loaded.dailyGoal, 0)
        }
    }

    private fun fallbackSettings(): GardenSettings = GardenSettings(
        targetSum = GameConfig.DEFAULT_TARGET,
        sessionSeconds = GameConfig.DEFAULT_SESSION_SECONDS,
        dailyGoal = GameConfig.DEFAULT_DAILY_GOAL,
        reducedMotion = false,
        gardenerName = ""
    )
}
