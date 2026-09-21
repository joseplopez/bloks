package com.centelles.bloks.engine

import com.centelles.bloks.engine.model.Board
import com.centelles.bloks.engine.model.Piece
import com.centelles.bloks.engine.model.Point
import kotlin.random.Random

class PieceGenerator {
    fun generateRoster(board: Board): List<Piece> {
        var roster: List<Piece>
        var attempts = 0
        
        do {
            roster = List(3) { Piece.Templates[Random.nextInt(Piece.Templates.size)] }
            attempts++
            // Garantizar que al menos una pieza sea colocable si el tablero no está bloqueado
            val canPlaceAny = roster.any { canFitAnywhere(board, it) }
        } while (!canPlaceAny && attempts < 50)
        
        return roster
    }

    fun canFitAnywhere(board: Board, piece: Piece): Boolean {
        for (x in 0 until board.size) {
            for (y in 0 until board.size) {
                if (board.canPlace(piece, Point(x, y))) return true
            }
        }
        return false
    }
}
