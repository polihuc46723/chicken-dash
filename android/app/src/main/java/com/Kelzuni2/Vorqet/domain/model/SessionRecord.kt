package com.Kelzuni2.Vorqet.domain.model

data class SessionRecord(
    val title: String,
    val score: Int,
    val blooms: Int,
    val accuracy: Int,
    val spriteIndex: Int = 0
)
