package com.Kelzuni2.Vorqet.domain.repository

import com.Kelzuni2.Vorqet.domain.model.DailyGoal
import com.Kelzuni2.Vorqet.domain.model.SessionRecord
import com.Kelzuni2.Vorqet.domain.model.SessionResult

interface SessionRepository {
    fun bestScore(): Int

    fun totalBlooms(): Int

    fun dailyGoal(goalSize: Int): DailyGoal

    fun history(): List<SessionRecord>

    fun persist(result: SessionResult, goalSize: Int)

    fun isNewBest(score: Int): Boolean
}
