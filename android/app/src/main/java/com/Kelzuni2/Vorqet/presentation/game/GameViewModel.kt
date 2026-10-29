package com.Kelzuni2.Vorqet.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelzuni2.Vorqet.core.config.GameConfig
import com.Kelzuni2.Vorqet.data.sample.SampleData
import com.Kelzuni2.Vorqet.domain.model.GardenSession
import com.Kelzuni2.Vorqet.domain.model.GardenSettings
import com.Kelzuni2.Vorqet.domain.model.PairOutcome
import com.Kelzuni2.Vorqet.domain.model.SessionResult
import com.Kelzuni2.Vorqet.domain.model.TileState
import com.Kelzuni2.Vorqet.domain.repository.SettingsRepository
import com.Kelzuni2.Vorqet.domain.usecase.FinishSessionUseCase
import com.Kelzuni2.Vorqet.domain.usecase.RefillGridUseCase
import com.Kelzuni2.Vorqet.domain.usecase.ResolvePairUseCase
import com.Kelzuni2.Vorqet.domain.usecase.StartSessionUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(
    private val startSession: StartSessionUseCase,
    private val resolvePair: ResolvePairUseCase,
    private val refillGrid: RefillGridUseCase,
    private val finishSession: FinishSessionUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GameUiState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    private val _finishEvent = MutableStateFlow<SessionResult?>(null)
    val finishEvent: StateFlow<SessionResult?> = _finishEvent.asStateFlow()

    private var settings: GardenSettings = defaultSettings()

    private var clockJob: Job? = null

    private var started = false

    fun start() {
        if (started) {
            return
        }
        started = true
        settings = loadSettings()
        val session = startSession(settings)
        _state.value = GameUiState(
            session = session,
            phase = GameUiState.Phase.LOADING,
            reducedMotion = settings.reducedMotion,
            targetSum = session.targetSum
        )
        viewModelScope.launch {
            delay(BOOT_DELAY_MS)
            if (_state.value.phase == GameUiState.Phase.LOADING) {
                _state.value = _state.value.copy(phase = GameUiState.Phase.PLAYING)
            }
            runClock()
        }
    }

    private fun loadSettings(): GardenSettings = try {
        settingsRepository.load()
    } catch (e: Exception) {
        defaultSettings()
    }

    private fun defaultSettings(): GardenSettings = GardenSettings(
        targetSum = GameConfig.DEFAULT_TARGET,
        sessionSeconds = GameConfig.DEFAULT_SESSION_SECONDS,
        dailyGoal = GameConfig.DEFAULT_DAILY_GOAL,
        reducedMotion = false,
        gardenerName = ""
    )

    private fun runClock() {
        clockJob?.cancel()
        clockJob = viewModelScope.launch {
            while (isActive) {
                delay(GameConfig.TICK_MS)
                val current = _state.value
                val session = current.session ?: continue
                if (current.phase == GameUiState.Phase.PAUSED ||
                    current.phase == GameUiState.Phase.FINISHED ||
                    current.phase == GameUiState.Phase.LOADING
                ) {
                    continue
                }
                val remaining = (session.millisLeft - GameConfig.TICK_MS).coerceAtLeast(0L)
                _state.value = current.copy(session = session.copy(millisLeft = remaining))
                if (remaining <= 0L) {
                    finishNow()
                    return@launch
                }
            }
        }
    }

    fun onTileTapped(index: Int) {
        val current = _state.value
        if (!current.isInteractive) {
            return
        }
        val session = current.session ?: return
        when (val outcome = resolvePair.tap(session, index)) {
            is PairOutcome.Ignored -> Unit

            is PairOutcome.Selected ->
                _state.value = current.copy(session = outcome.session)

            is PairOutcome.Deselected ->
                _state.value = current.copy(session = outcome.session)

            is PairOutcome.Matched -> handleMatched(current, outcome)

            is PairOutcome.Missed -> handleMissed(current, outcome)
        }
    }

    private fun handleMatched(current: GameUiState, outcome: PairOutcome.Matched) {
        _state.value = current.copy(
            session = outcome.session,
            phase = GameUiState.Phase.RESOLVING
        )
        viewModelScope.launch {
            delay(GameConfig.RESOLVE_DELAY_MS)
            val latest = _state.value
            if (latest.phase == GameUiState.Phase.FINISHED) {
                return@launch
            }
            val session = latest.session ?: return@launch
            val refreshed = refillGrid.refill(
                session.tiles,
                listOf(outcome.first, outcome.second),
                session.targetSum
            )
            _state.value = latest.copy(
                session = session.copy(tiles = refreshed),
                phase = resumePhase(latest.phase)
            )
        }
    }

    private fun handleMissed(current: GameUiState, outcome: PairOutcome.Missed) {
        _state.value = current.copy(
            session = outcome.session,
            phase = GameUiState.Phase.RESOLVING
        )
        viewModelScope.launch {
            delay(GameConfig.SHAKE_DURATION_MS)
            val latest = _state.value
            if (latest.phase == GameUiState.Phase.FINISHED) {
                return@launch
            }
            val session = latest.session ?: return@launch
            val cleared = session.tiles.map { tile ->
                if (tile.state == TileState.WRONG) {
                    tile.copy(state = TileState.IDLE)
                } else {
                    tile
                }
            }
            _state.value = latest.copy(
                session = session.copy(tiles = cleared),
                phase = resumePhase(latest.phase)
            )
        }
    }

    private fun resumePhase(phase: GameUiState.Phase): GameUiState.Phase =
        if (phase == GameUiState.Phase.PAUSED) {
            GameUiState.Phase.PAUSED
        } else {
            GameUiState.Phase.PLAYING
        }

    fun reshuffle() {
        val current = _state.value
        if (!current.isInteractive) {
            return
        }
        val session = current.session ?: return
        val tiles = refillGrid.reshuffle(session.tiles, session.targetSum)
        val penalised = (session.score - GameConfig.RESHUFFLE_COST).coerceAtLeast(0)
        _state.value = current.copy(
            session = session.copy(
                tiles = tiles,
                score = penalised,
                selected = emptyList(),
                combo = 1.0
            )
        )
    }

    fun pause() {
        val current = _state.value
        if (current.phase == GameUiState.Phase.FINISHED) {
            return
        }
        _state.value = current.copy(phase = GameUiState.Phase.PAUSED)
    }

    fun resume() {
        val current = _state.value
        if (current.phase != GameUiState.Phase.PAUSED) {
            return
        }
        _state.value = current.copy(phase = GameUiState.Phase.PLAYING)
    }

    private fun finishNow() {
        val current = _state.value
        if (current.phase == GameUiState.Phase.FINISHED) {
            return
        }
        val session = current.session ?: return
        _state.value = current.copy(phase = GameUiState.Phase.FINISHED)
        clockJob?.cancel()
        clockJob = null
        val result = buildResult(session)
        _finishEvent.value = result
    }

    private fun buildResult(session: GardenSession): SessionResult {
        val plant = SampleData.plantFor(session.correctPairs % SampleData.PLANT_NAMES.size)
        return try {
            finishSession(session, settings.dailyGoal, plant)
        } catch (e: Exception) {
            SessionResult(
                score = session.score,
                blooms = session.blooms,
                bestCombo = session.bestCombo,
                bestPair = session.bestPairPoints,
                accuracy = session.accuracy,
                isNewBest = false,
                plantName = plant
            )
        }
    }

    fun consumeFinish() {
        _finishEvent.value = null
    }

    override fun onCleared() {
        clockJob?.cancel()
        clockJob = null
        super.onCleared()
    }

    companion object {
        private const val BOOT_DELAY_MS = 180L
    }
}
