package com.centelles.bloks.engine.logic

import com.centelles.bloks.engine.model.Board
import com.centelles.bloks.engine.model.Piece
import com.centelles.bloks.engine.model.Point
import kotlin.random.Random

class PieceGenerator(private val seed: Long? = null) {
    private val random = seed?.let { Random(it) } ?: Random.Default

    fun generateRndPieces(board: Board): List<Piece> {
        val pieces = mutableListOf<Piece>()
        
        // Generate 3 pieces
        // For the first two, just pick random ones
        repeat(2) {
            pieces.add(Piece.Templates[random.nextInt(Piece.Templates.size)])
        }

        // For the 3rd piece, ensure fairness: at least one of the 3 pieces must be placeable
        val anyFits = pieces.any { p -> boardFitsPiece(board, p) }
        
        if (anyFits) {
            pieces.add(Piece.Templates[random.nextInt(Piece.Templates.size)])
        } else {
            // Find all pieces that fit
            val fittingTemplates = Piece.Templates.filter { p -> boardFitsPiece(board, p) }
            if (fittingTemplates.isNotEmpty()) {
                pieces.add(fittingTemplates[random.nextInt(fittingTemplates.size)])
            } else {
                // No pieces fit at all, just pick a random one (game over will trigger soon)
                pieces.add(Piece.Templates[random.nextInt(Piece.Templates.size)])
            }
        }
        
        return pieces.shuffled(random)
    }

    private fun boardFitsPiece(board: Board, piece: Piece): Boolean {
        for (x in 0 until board.size) {
            for (y in 0 until board.size) {
                if (board.canPlace(piece, Point(x, y))) return true
            }
        }
        return false
    }
}
