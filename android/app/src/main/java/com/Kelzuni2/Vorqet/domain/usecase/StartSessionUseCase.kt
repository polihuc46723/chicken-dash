package com.Kelzuni2.Vorqet.domain.usecase

import com.Kelzuni2.Vorqet.domain.model.GardenSession
import com.Kelzuni2.Vorqet.domain.model.GardenSettings

class StartSessionUseCase(private val refillGrid: RefillGridUseCase) {

    operator fun invoke(settings: GardenSettings): GardenSession {
        val total = settings.sessionSeconds * 1000L
        return GardenSession(
            tiles = refillGrid.newGrid(settings.targetSum),
            targetSum = settings.targetSum,
            millisLeft = total,
            totalMillis = total
        )
    }
}
