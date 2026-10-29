package com.Kelzuni2.Vorqet.domain.usecase

import com.Kelzuni2.Vorqet.domain.model.GardenSession
import com.Kelzuni2.Vorqet.domain.model.SessionResult
import com.Kelzuni2.Vorqet.domain.repository.SessionRepository

class FinishSessionUseCase(private val repository: SessionRepository) {

    operator fun invoke(
        session: GardenSession,
        goalSize: Int,
        plantName: String
    ): SessionResult {
        val result = SessionResult(
            score = session.score,
            blooms = session.blooms,
            bestCombo = session.bestCombo,
            bestPair = session.bestPairPoints,
            accuracy = session.accuracy,
            isNewBest = repository.isNewBest(session.score),
            plantName = plantName
        )
        repository.persist(result, goalSize)
        return result
    }
}
