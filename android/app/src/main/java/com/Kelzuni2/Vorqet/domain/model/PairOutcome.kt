package com.Kelzuni2.Vorqet.domain.model

sealed class PairOutcome {
    object Ignored : PairOutcome()

    data class Selected(val session: GardenSession) : PairOutcome()

    data class Deselected(val session: GardenSession) : PairOutcome()

    data class Matched(
        val session: GardenSession,
        val first: Int,
        val second: Int,
        val points: Int
    ) : PairOutcome()

    data class Missed(
        val session: GardenSession,
        val first: Int,
        val second: Int
    ) : PairOutcome()
}
