package com.centelles.bloks.engine.logic

import com.centelles.bloks.engine.model.*

class GameEngine(
    private val pieceGenerator: PieceGenerator = PieceGenerator()
) {
    private var state = GameState()

    fun getGameState(): GameState = state

    fun startNewGame() {
        val board = Board()
        val initialPieces = pieceGenerator.generateRndPieces(board)
        state = GameState(
            board = board,
            availablePieces = initialPieces
        )
    }

    /**
     * Tries to place a piece from the available pieces at the given board coordinates.
     * Returns true if successful.
     */
    fun placePiece(pieceIndex: Int, position: Point): Boolean {
        if (state.isGameOver) return false
        
        val piece = state.availablePieces.getOrNull(pieceIndex) ?: return false
        val board = state.board

        if (!board.canPlace(piece, position)) return false

        // 1. Place the piece
        board.place(piece, position)
        
        // 2. Calculate score for placement
        var newScore = state.score + piece.blocks.size
        
        // 3. Check for cleared lines
        val linesCleared = board.clearFullLines()
        var newStreak = state.streak
        
        if (linesCleared > 0) {
            // Combo (multiple lines) + Streak (consecutive moves)
            val moveBonus = (linesCleared * 10 * linesCleared) * (newStreak + 1)
            newScore += moveBonus
            newStreak++
        } else {
            newStreak = 0
        }

        // 4. Update available pieces
        val newAvailablePieces = state.availablePieces.toMutableList()
        newAvailablePieces[pieceIndex] = null
        
        // 5. Check if we need to generate new pieces (all 3 used)
        if (newAvailablePieces.all { it == null }) {
            val nextPieces = pieceGenerator.generateRndPieces(board)
            nextPieces.forEachIndexed { i, p -> newAvailablePieces[i] = p }
        }

        // 6. Check Game Over
        val isGameOver = !board.hasAnyPossibleMove(newAvailablePieces)

        state = state.copy(
            score = newScore,
            streak = newStreak,
            availablePieces = newAvailablePieces,
            isGameOver = isGameOver
        )
        
        return true
    }

    /**
     * Resets game over state and clears some space to allow the player to continue.
     */
    fun continueAfterGameOver() {
        if (!state.isGameOver) return

        // Clear a 4x4 area in the center to give space
        for (x in 2..5) {
            for (y in 2..5) {
                state.board.grid[x][y] = null
            }
        }

        // Generate new pieces that are guaranteed to fit if possible
        val newPieces = pieceGenerator.generateRndPieces(state.board)
        
        state = state.copy(
            isGameOver = false,
            availablePieces = newPieces
        )
    }

    // --- POWER-UPS ---

    fun rotatePiece(pieceIndex: Int) {
        val piece = state.availablePieces.getOrNull(pieceIndex) ?: return
        val newAvailablePieces = state.availablePieces.toMutableList()
        newAvailablePieces[pieceIndex] = piece.rotated()
        
        state = state.copy(
            availablePieces = newAvailablePieces,
            isGameOver = !state.board.hasAnyPossibleMove(newAvailablePieces)
        )
    }

    fun shufflePieces() {
        val nextPieces = pieceGenerator.generateRndPieces(state.board)
        state = state.copy(
            availablePieces = nextPieces,
            isGameOver = !state.board.hasAnyPossibleMove(nextPieces)
        )
    }

    fun clearArea(center: Point) {
        // Clear a 3x3 area around center
        for (x in (center.x - 1)..(center.x + 1)) {
            for (y in (center.y - 1)..(center.y + 1)) {
                if (x in 0 until state.board.size && y in 0 until state.board.size) {
                    state.board.grid[x][y] = null
                }
            }
        }
        
        state = state.copy(
            isGameOver = !state.board.hasAnyPossibleMove(state.availablePieces)
        )
    }
}
