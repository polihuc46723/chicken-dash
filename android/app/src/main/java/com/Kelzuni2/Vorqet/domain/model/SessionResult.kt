package com.Kelzuni2.Vorqet.domain.model

data class SessionResult(
    val score: Int,
    val blooms: Int,
    val bestCombo: Double,
    val bestPair: Int,
    val accuracy: Int,
    val isNewBest: Boolean,
    val plantName: String
)
