package com.Kelzuni2.Vorqet.data.repository

import com.Kelzuni2.Vorqet.core.config.GameConfig
import com.Kelzuni2.Vorqet.data.local.GardenPreferences
import com.Kelzuni2.Vorqet.data.sample.SampleData
import com.Kelzuni2.Vorqet.domain.model.DailyGoal
import com.Kelzuni2.Vorqet.domain.model.SessionRecord
import com.Kelzuni2.Vorqet.domain.model.SessionResult
import com.Kelzuni2.Vorqet.domain.repository.SessionRepository

class SessionRepositoryImpl(
    private val preferences: GardenPreferences
) : SessionRepository {

    override fun bestScore(): Int = preferences.bestScore()

    override fun totalBlooms(): Int = preferences.totalBlooms()

    override fun dailyGoal(goalSize: Int): DailyGoal = DailyGoal(
        done = preferences.goalBlooms(),
        goal = goalSize,
        streakDays = preferences.streakDays()
    )

    override fun history(): List<SessionRecord> {
        val raw = preferences.historyRaw()
        if (raw.isBlank()) {
            return SampleData.SEED_HISTORY
        }
        return raw.split(ROW_SEPARATOR)
            .mapNotNull { decode(it) }
            .take(GameConfig.HISTORY_LIMIT)
    }

    override fun persist(result: SessionResult, goalSize: Int) {
        if (result.score > preferences.bestScore()) {
            preferences.setBestScore(result.score)
        }
        preferences.addBlooms(result.blooms)
        preferences.addGoalBlooms(result.blooms)
        if (preferences.goalBlooms() >= goalSize) {
            preferences.markStreakMet()
        }
        val stored = preferences.historyRaw()
            .split(ROW_SEPARATOR)
            .filter { it.isNotBlank() }
        val record = SessionRecord(
            title = result.plantName + " bed",
            score = result.score,
            blooms = result.blooms,
            accuracy = result.accuracy,
            spriteIndex = SampleData.PLANT_NAMES.indexOf(result.plantName).coerceAtLeast(0)
        )
        val merged = (listOf(encode(record)) + stored).take(GameConfig.HISTORY_LIMIT)
        preferences.setHistoryRaw(merged.joinToString(ROW_SEPARATOR))
    }

    override fun isNewBest(score: Int): Boolean = score > preferences.bestScore()

    private fun encode(record: SessionRecord): String = listOf(
        record.title.replace(FIELD_SEPARATOR, " ").replace(ROW_SEPARATOR, " "),
        record.score.toString(),
        record.blooms.toString(),
        record.accuracy.toString(),
        record.spriteIndex.toString()
    ).joinToString(FIELD_SEPARATOR)

    private fun decode(row: String): SessionRecord? {
        val parts = row.split(FIELD_SEPARATOR)
        if (parts.size < 5) {
            return null
        }
        val score = parts[1].toIntOrNull() ?: return null
        val blooms = parts[2].toIntOrNull() ?: return null
        val accuracy = parts[3].toIntOrNull() ?: return null
        val sprite = parts[4].toIntOrNull() ?: 0
        return SessionRecord(parts[0], score, blooms, accuracy, sprite)
    }

    companion object {
        private const val ROW_SEPARATOR = ";"
        private const val FIELD_SEPARATOR = "|"
    }
}
