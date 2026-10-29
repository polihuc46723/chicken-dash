package com.Kelzuni2.Vorqet.domain.usecase

import com.Kelzuni2.Vorqet.core.config.GameConfig
import com.Kelzuni2.Vorqet.domain.model.GardenSession
import com.Kelzuni2.Vorqet.domain.model.PairOutcome
import com.Kelzuni2.Vorqet.domain.model.TileState
import kotlin.math.roundToInt
import kotlin.random.Random

class ResolvePairUseCase(private val random: Random = Random.Default) {

    fun tap(session: GardenSession, index: Int): PairOutcome {
        val tile = session.tiles.getOrNull(index) ?: return PairOutcome.Ignored
        if (tile.state == TileState.BLOOMED) {
            return PairOutcome.Ignored
        }
        if (session.selected.contains(index)) {
            val tiles = session.tiles.map { current ->
                if (current.index == index) current.copy(state = TileState.IDLE) else current
            }
            return PairOutcome.Deselected(
                session.copy(tiles = tiles, selected = session.selected - index)
            )
        }
        if (session.selected.isEmpty()) {
            val tiles = session.tiles.map { current ->
                if (current.index == index) current.copy(state = TileState.SELECTED) else current
            }
            return PairOutcome.Selected(
                session.copy(tiles = tiles, selected = listOf(index))
            )
        }
        if (session.selected.size >= 2) {
            return PairOutcome.Ignored
        }
        val firstIndex = session.selected.first()
        val firstTile = session.tiles.getOrNull(firstIndex) ?: return PairOutcome.Ignored
        val sum = firstTile.value + tile.value
        if (sum == session.targetSum) {
            return matched(session, firstIndex, index)
        }
        return missed(session, firstIndex, index)
    }

    private fun matched(session: GardenSession, first: Int, second: Int): PairOutcome {
        val points = (GameConfig.BASE_PAIR_POINTS * session.combo).roundToInt()
        val nextCombo = nextCombo(session.combo)
        val spriteA = random.nextInt(GameConfig.SPRITE_COUNT)
        val spriteB = random.nextInt(GameConfig.SPRITE_COUNT)
        val tiles = session.tiles.map { current ->
            when (current.index) {
                first -> current.copy(state = TileState.BLOOMED, spriteIndex = spriteA)
                second -> current.copy(state = TileState.BLOOMED, spriteIndex = spriteB)
                else -> current
            }
        }
        val updated = session.copy(
            tiles = tiles,
            selected = emptyList(),
            score = session.score + points,
            combo = nextCombo,
            bestCombo = maxOf(session.bestCombo, nextCombo),
            correctPairs = session.correctPairs + 1,
            bestPairPoints = maxOf(session.bestPairPoints, points)
        )
        return PairOutcome.Matched(updated, first, second, points)
    }

    private fun missed(session: GardenSession, first: Int, second: Int): PairOutcome {
        val tiles = session.tiles.map { current ->
            when (current.index) {
                first, second -> current.copy(state = TileState.WRONG)
                else -> current
            }
        }
        val updated = session.copy(
            tiles = tiles,
            selected = emptyList(),
            combo = 1.0,
            mistakes = session.mistakes + 1
        )
        return PairOutcome.Missed(updated, first, second)
    }

    private fun nextCombo(current: Double): Double = when {
        current < 1.2 -> 1.2
        current < 1.5 -> 1.5
        else -> 2.0
    }
}
