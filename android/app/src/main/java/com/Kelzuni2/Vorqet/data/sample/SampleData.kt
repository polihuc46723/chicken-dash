package com.Kelzuni2.Vorqet.data.sample

import com.Kelzuni2.Vorqet.domain.model.SessionRecord

object SampleData {

    val PLANT_NAMES = listOf(
        "Sunbell",
        "Mint Fern",
        "Clover Knot",
        "Dawn Poppy",
        "Paper Iris"
    )

    val SEED_HISTORY = listOf(
        SessionRecord("Mint Fern bed", 180, 14, 78, 1),
        SessionRecord("Clover Knot bed", 240, 18, 85, 2),
        SessionRecord("Dawn Poppy bed", 120, 10, 71, 3)
    )

    val TIPS = listOf(
        "Scan the bed for nines first, they pair with the smallest seeds.",
        "A clean streak lifts the multiplier to double points.",
        "New sprouts cost five points, use them when the bed looks tired.",
        "Accuracy counts more than speed in a short garden session."
    )

    fun plantFor(index: Int): String = PLANT_NAMES[index.coerceIn(0, PLANT_NAMES.size - 1)]

    fun bedNameFor(index: Int): String = plantFor(index) + " bed"

    fun tipFor(index: Int): String = TIPS[index.coerceIn(0, TIPS.size - 1)]
}
