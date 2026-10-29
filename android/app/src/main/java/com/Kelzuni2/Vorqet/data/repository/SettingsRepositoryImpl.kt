package com.Kelzuni2.Vorqet.data.repository

import com.Kelzuni2.Vorqet.data.local.GardenPreferences
import com.Kelzuni2.Vorqet.domain.model.GardenSettings
import com.Kelzuni2.Vorqet.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val preferences: GardenPreferences
) : SettingsRepository {

    override fun load(): GardenSettings = GardenSettings(
        targetSum = preferences.targetSum(),
        sessionSeconds = preferences.sessionSeconds(),
        dailyGoal = preferences.dailyGoal(),
        reducedMotion = preferences.reducedMotion(),
        gardenerName = preferences.gardenerName()
    )

    override fun save(settings: GardenSettings) {
        preferences.saveSettings(
            target = settings.targetSum,
            seconds = settings.sessionSeconds,
            goal = settings.dailyGoal,
            reducedMotion = settings.reducedMotion,
            name = settings.gardenerName
        )
    }
}
