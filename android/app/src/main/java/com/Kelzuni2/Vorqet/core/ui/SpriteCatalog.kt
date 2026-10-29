package com.Kelzuni2.Vorqet.core.ui

import com.Kelzuni2.Vorqet.R

object SpriteCatalog {

    private val SPRITES = intArrayOf(
        R.drawable.sprite_1,
        R.drawable.sprite_2,
        R.drawable.sprite_3,
        R.drawable.sprite_4,
        R.drawable.sprite_5
    )

    val size: Int
        get() = SPRITES.size

    fun drawableAt(index: Int): Int = SPRITES[index.coerceIn(0, SPRITES.size - 1)]
}
