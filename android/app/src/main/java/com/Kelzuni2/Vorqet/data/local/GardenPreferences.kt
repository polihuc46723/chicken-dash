package com.Kelzuni2.Vorqet.data.local

import android.content.Context
import android.content.SharedPreferences
import com.Kelzuni2.Vorqet.core.config.GameConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GardenPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)

    fun bestScore(): Int = prefs.getInt(KEY_BEST_SCORE, 0)

    fun setBestScore(value: Int) {
        prefs.edit().putInt(KEY_BEST_SCORE, value).apply()
    }

    fun totalBlooms(): Int = prefs.getInt(KEY_TOTAL_BLOOMS, 0)

    fun addBlooms(value: Int) {
        prefs.edit().putInt(KEY_TOTAL_BLOOMS, totalBlooms() + value).apply()
    }

    fun goalBlooms(): Int {
        if (prefs.getString(KEY_GOAL_DATE, "") != today()) {
            return 0
        }
        return prefs.getInt(KEY_GOAL_BLOOMS, 0)
    }

    fun addGoalBlooms(value: Int) {
        val current = goalBlooms()
        prefs.edit()
            .putString(KEY_GOAL_DATE, today())
            .putInt(KEY_GOAL_BLOOMS, current + value)
            .apply()
    }

    fun streakDays(): Int = prefs.getInt(KEY_STREAK_DAYS, 0)

    fun markStreakMet() {
        if (prefs.getString(KEY_STREAK_DATE, "") == today()) {
            return
        }
        prefs.edit()
            .putString(KEY_STREAK_DATE, today())
            .putInt(KEY_STREAK_DAYS, streakDays() + 1)
            .apply()
    }

    fun historyRaw(): String = prefs.getString(KEY_HISTORY, "").orEmpty()

    fun setHistoryRaw(value: String) {
        prefs.edit().putString(KEY_HISTORY, value).apply()
    }

    fun targetSum(): Int = prefs.getInt(KEY_TARGET, GameConfig.DEFAULT_TARGET)

    fun sessionSeconds(): Int = prefs.getInt(KEY_SECONDS, GameConfig.DEFAULT_SESSION_SECONDS)

    fun dailyGoal(): Int = prefs.getInt(KEY_DAILY_GOAL, GameConfig.DEFAULT_DAILY_GOAL)

    fun reducedMotion(): Boolean = prefs.getBoolean(KEY_REDUCED_MOTION, false)

    fun gardenerName(): String = prefs.getString(KEY_NAME, "").orEmpty()

    fun saveSettings(
        target: Int,
        seconds: Int,
        goal: Int,
        reducedMotion: Boolean,
        name: String
    ) {
        prefs.edit()
            .putInt(KEY_TARGET, target)
            .putInt(KEY_SECONDS, seconds)
            .putInt(KEY_DAILY_GOAL, goal)
            .putBoolean(KEY_REDUCED_MOTION, reducedMotion)
            .putString(KEY_NAME, name)
            .apply()
    }

    private fun today(): String =
        SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())

    companion object {
        private const val STORE_NAME = "number_garden_store"
        private const val KEY_BEST_SCORE = "best_score"
        private const val KEY_TOTAL_BLOOMS = "total_blooms"
        private const val KEY_GOAL_DATE = "goal_date"
        private const val KEY_GOAL_BLOOMS = "goal_blooms"
        private const val KEY_STREAK_DAYS = "streak_days"
        private const val KEY_STREAK_DATE = "streak_date"
        private const val KEY_HISTORY = "history_json"
        private const val KEY_TARGET = "target_sum"
        private const val KEY_SECONDS = "session_seconds"
        private const val KEY_DAILY_GOAL = "daily_goal"
        private const val KEY_REDUCED_MOTION = "reduced_motion"
        private const val KEY_NAME = "gardener_name"
    }
}
