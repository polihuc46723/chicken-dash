package com.Kelzuni2.Vorqet.domain.model

data class DailyGoal(
    val done: Int,
    val goal: Int,
    val streakDays: Int
) {
    val percent: Int
        get() = if (goal <= 0) 0 else ((done.toDouble() / goal) * 100).toInt().coerceIn(0, 100)
}
