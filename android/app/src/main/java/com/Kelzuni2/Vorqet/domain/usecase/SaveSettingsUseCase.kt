package com.Kelzuni2.Vorqet.domain.usecase

import com.Kelzuni2.Vorqet.domain.model.GardenSettings
import com.Kelzuni2.Vorqet.domain.repository.SettingsRepository

class SaveSettingsUseCase(private val repository: SettingsRepository) {

    operator fun invoke(settings: GardenSettings) {
        repository.save(settings)
    }

    fun current(): GardenSettings = repository.load()
}
