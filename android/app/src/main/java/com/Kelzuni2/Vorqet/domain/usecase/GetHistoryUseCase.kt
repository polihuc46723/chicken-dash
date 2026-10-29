package com.Kelzuni2.Vorqet.domain.usecase

import com.Kelzuni2.Vorqet.domain.model.SessionRecord
import com.Kelzuni2.Vorqet.domain.repository.SessionRepository

class GetHistoryUseCase(private val repository: SessionRepository) {

    operator fun invoke(): List<SessionRecord> = repository.history()
}
