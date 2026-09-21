package com.centelles.bloks.engine.model

data class GameState(
    val score: Int = 0,
    val streak: Int = 0,
    val availablePieces: List<Piece?> = listOf(null, null, null),
    val isGameOver: Boolean = false,
    val coins: Int = 0,
    val board: Board = Board()
)
