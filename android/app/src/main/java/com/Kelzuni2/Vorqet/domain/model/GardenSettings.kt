package com.Kelzuni2.Vorqet.domain.model

data class GardenSettings(
    val targetSum: Int,
    val sessionSeconds: Int,
    val dailyGoal: Int,
    val reducedMotion: Boolean,
    val gardenerName: String
)
