package com.Kelzuni2.Vorqet.domain.model

data class NumberTile(
    val index: Int,
    val value: Int,
    val state: TileState = TileState.IDLE,
    val spriteIndex: Int = 0
)
