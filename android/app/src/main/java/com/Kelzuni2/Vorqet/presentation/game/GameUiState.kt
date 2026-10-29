package com.Kelzuni2.Vorqet.presentation.game

import com.Kelzuni2.Vorqet.domain.model.GardenSession

data class GameUiState(
    val session: GardenSession? = null,
    val phase: Phase = Phase.LOADING,
    val reducedMotion: Boolean = false,
    val targetSum: Int = 0
) {
    val isInteractive: Boolean
        get() = phase == Phase.PLAYING

    enum class Phase {
        LOADING,
        PLAYING,
        RESOLVING,
        PAUSED,
        FINISHED
    }
}
