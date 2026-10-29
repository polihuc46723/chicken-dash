package com.Kelzuni2.Vorqet.core.di

import android.content.Context
import com.Kelzuni2.Vorqet.data.local.GardenPreferences
import com.Kelzuni2.Vorqet.data.repository.SessionRepositoryImpl
import com.Kelzuni2.Vorqet.data.repository.SettingsRepositoryImpl
import com.Kelzuni2.Vorqet.domain.repository.SessionRepository
import com.Kelzuni2.Vorqet.domain.repository.SettingsRepository
import com.Kelzuni2.Vorqet.domain.usecase.FinishSessionUseCase
import com.Kelzuni2.Vorqet.domain.usecase.GetDailyProgressUseCase
import com.Kelzuni2.Vorqet.domain.usecase.GetHistoryUseCase
import com.Kelzuni2.Vorqet.domain.usecase.RefillGridUseCase
import com.Kelzuni2.Vorqet.domain.usecase.ResolvePairUseCase
import com.Kelzuni2.Vorqet.domain.usecase.SaveSettingsUseCase
import com.Kelzuni2.Vorqet.domain.usecase.StartSessionUseCase

object ServiceLocator {

    private var preferences: GardenPreferences? = null
    private var sessionRepositoryRef: SessionRepository? = null
    private var settingsRepositoryRef: SettingsRepository? = null
    private var refillGridRef: RefillGridUseCase? = null

    fun init(context: Context) {
        if (preferences == null) {
            val store = GardenPreferences(context.applicationContext)
            preferences = store
            sessionRepositoryRef = SessionRepositoryImpl(store)
            settingsRepositoryRef = SettingsRepositoryImpl(store)
            refillGridRef = RefillGridUseCase()
        }
    }

    val isReady: Boolean
        get() = preferences != null

    val sessionRepository: SessionRepository
        get() = requireNotNull(sessionRepositoryRef)

    val settingsRepository: SettingsRepository
        get() = requireNotNull(settingsRepositoryRef)

    val refillGrid: RefillGridUseCase
        get() = requireNotNull(refillGridRef)

    val resolvePair: ResolvePairUseCase
        get() = ResolvePairUseCase()

    val startSession: StartSessionUseCase
        get() = StartSessionUseCase(refillGrid)

    val finishSession: FinishSessionUseCase
        get() = FinishSessionUseCase(sessionRepository)

    val dailyProgress: GetDailyProgressUseCase
        get() = GetDailyProgressUseCase(sessionRepository)

    val history: GetHistoryUseCase
        get() = GetHistoryUseCase(sessionRepository)

    val saveSettings: SaveSettingsUseCase
        get() = SaveSettingsUseCase(settingsRepository)
}
