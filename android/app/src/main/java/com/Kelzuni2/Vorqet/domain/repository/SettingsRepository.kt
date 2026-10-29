package com.Kelzuni2.Vorqet.domain.repository

import com.Kelzuni2.Vorqet.domain.model.GardenSettings

interface SettingsRepository {
    fun load(): GardenSettings

    fun save(settings: GardenSettings)
}
