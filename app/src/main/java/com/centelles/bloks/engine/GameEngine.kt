package com.centelles.bloks.engine

import com.centelles.bloks.engine.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameEngine @Inject constructor(
    private val pieceGenerator: PieceGenerator
) {
    var board = Board()
        private set
    
    var state = GameState()
        private set

    fun startNewGame() {
        board = Board()
        val initialPieces = pieceGenerator.generateRoster(board)
        state = GameState(availablePieces = initialPieces)
    }

    fun playTurn(pieceIndex: Int, at: Point): Boolean {
        val piece = state.availablePieces.getOrNull(pieceIndex) ?: return false
        
        if (!board.canPlace(piece, at)) return false

        // 1. Colocar pieza
        board.place(piece, at)
        
        // 2. Calcular puntos base (10 pts por bloque)
        val basePoints = piece.blocks.size * 10
        
        // 3. Limpiar líneas y calcular bonos
        val linesCleared = board.clearFullLines()
        val linePoints = linesCleared * 100
        
        var newScore = state.score + basePoints + linePoints
        var newStreak = if (linesCleared > 0) state.streak + 1 else 0
        
        // Bonus por combo y streak
        if (linesCleared > 1) newScore += (linesCleared * 50) 
        if (newStreak > 1) newScore += (newStreak * 100)

        // 4. Actualizar piezas disponibles
        val newAvailable = state.availablePieces.toMutableList()
        newAvailable[pieceIndex] = null
        
        // Si no quedan piezas, generar nuevas
        val finalAvailable = if (newAvailable.all { it == null }) {
            pieceGenerator.generateRoster(board)
        } else {
            newAvailable
        }

        // 5. Verificar Game Over
        val gameOver = finalAvailable.filterNotNull().none { pieceGenerator.canFitAnywhere(board, it) }

        state = state.copy(
            score = newScore,
            streak = newStreak,
            availablePieces = finalAvailable,
            isGameOver = gameOver
        )
        
        return true
    }
}
