package com.Kelzuni2.Vorqet.domain.usecase

import com.Kelzuni2.Vorqet.core.config.GameConfig
import com.Kelzuni2.Vorqet.domain.model.NumberTile
import com.Kelzuni2.Vorqet.domain.model.TileState
import kotlin.random.Random

class RefillGridUseCase(private val random: Random = Random.Default) {

    fun newGrid(targetSum: Int): List<NumberTile> {
        val tiles = (0 until GameConfig.GRID_SIZE).map { index ->
            NumberTile(index = index, value = randomValue(), state = TileState.IDLE)
        }
        return ensureSolvable(tiles, targetSum)
    }

    fun refill(tiles: List<NumberTile>, indices: List<Int>, targetSum: Int): List<NumberTile> {
        val mutable = tiles.toMutableList()
        indices.forEach { index ->
            if (index in mutable.indices) {
                mutable[index] = mutable[index].copy(
                    value = randomValue(),
                    state = TileState.IDLE,
                    spriteIndex = 0
                )
            }
        }
        return ensureSolvable(mutable, targetSum)
    }

    fun reshuffle(tiles: List<NumberTile>, targetSum: Int): List<NumberTile> {
        val mutable = tiles.map { tile ->
            tile.copy(value = randomValue(), state = TileState.IDLE, spriteIndex = 0)
        }
        return ensureSolvable(mutable, targetSum)
    }

    fun hasValidPair(tiles: List<NumberTile>, targetSum: Int): Boolean {
        val playable = tiles.filter { it.state != TileState.BLOOMED }
        for (i in playable.indices) {
            for (j in i + 1 until playable.size) {
                if (playable[i].value + playable[j].value == targetSum) {
                    return true
                }
            }
        }
        return false
    }

    fun ensureSolvable(tiles: List<NumberTile>, targetSum: Int): List<NumberTile> {
        if (hasValidPair(tiles, targetSum)) {
            return tiles
        }
        val mutable = tiles.toMutableList()
        if (mutable.size < 2) {
            return mutable
        }
        val first = random.nextInt(mutable.size)
        var second = random.nextInt(mutable.size)
        while (second == first) {
            second = random.nextInt(mutable.size)
        }
        val low = (targetSum - GameConfig.MAX_TILE_VALUE)
            .coerceIn(GameConfig.MIN_TILE_VALUE, GameConfig.MAX_TILE_VALUE)
        val high = (targetSum - low)
            .coerceIn(GameConfig.MIN_TILE_VALUE, GameConfig.MAX_TILE_VALUE)
        mutable[first] = mutable[first].copy(value = low, state = TileState.IDLE, spriteIndex = 0)
        mutable[second] = mutable[second].copy(value = high, state = TileState.IDLE, spriteIndex = 0)
        return mutable
    }

    private fun randomValue(): Int =
        random.nextInt(GameConfig.MIN_TILE_VALUE, GameConfig.MAX_TILE_VALUE + 1)
}
