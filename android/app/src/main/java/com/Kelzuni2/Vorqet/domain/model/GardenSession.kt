package com.Kelzuni2.Vorqet.domain.model

data class GardenSession(
    val tiles: List<NumberTile>,
    val targetSum: Int,
    val score: Int = 0,
    val combo: Double = 1.0,
    val bestCombo: Double = 1.0,
    val correctPairs: Int = 0,
    val mistakes: Int = 0,
    val bestPairPoints: Int = 0,
    val millisLeft: Long = 0L,
    val totalMillis: Long = 0L,
    val selected: List<Int> = emptyList()
) {
    val blooms: Int
        get() = correctPairs * 2

    val accuracy: Int
        get() {
            val attempts = correctPairs + mistakes
            return if (attempts == 0) 0 else ((correctPairs.toDouble() / attempts) * 100).toInt()
        }

    val secondsLeft: Int
        get() = ((millisLeft + 999L) / 1000L).toInt().coerceAtLeast(0)

    val timePercent: Int
        get() = if (totalMillis <= 0L) 0 else ((millisLeft.toDouble() / totalMillis) * 100).toInt().coerceIn(0, 100)
}
