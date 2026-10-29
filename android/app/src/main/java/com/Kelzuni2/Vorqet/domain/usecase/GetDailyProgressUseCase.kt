package com.Kelzuni2.Vorqet.domain.usecase

import com.Kelzuni2.Vorqet.domain.model.DailyGoal
import com.Kelzuni2.Vorqet.domain.repository.SessionRepository

class GetDailyProgressUseCase(private val repository: SessionRepository) {

    operator fun invoke(goalSize: Int): DailyGoal = repository.dailyGoal(goalSize)
}
