package com.centelles.bloks.engine

import com.centelles.bloks.engine.model.Board
import com.centelles.bloks.engine.model.Piece
import com.centelles.bloks.engine.model.Point
import org.junit.Assert.*
import org.junit.Test

class BoardTest {

    @Test
    fun `placing piece updates grid`() {
        val board = Board(8)
        val piece = Piece.Templates.first { it.id == "1x1" }
        
        board.place(piece, Point(0, 0))
        assertNotNull(board.grid[0][0])
        assertEquals(piece.color, board.grid[0][0])
    }

    @Test
    fun `cannot place piece outside bounds`() {
        val board = Board(8)
        val piece = Piece.Templates.first { it.id == "1x1" }
        
        assertFalse(board.canPlace(piece, Point(-1, 0)))
        assertFalse(board.canPlace(piece, Point(8, 0)))
    }

    @Test
    fun `clearing a full row works`() {
        val board = Board(8)
        val piece = Piece.Templates.first { it.id == "1x1" }
        
        for (x in 0 until 8) {
            board.place(piece, Point(x, 0))
        }
        
        val linesCleared = board.clearFullLines()
        assertEquals(1, linesCleared)
        for (x in 0 until 8) {
            assertNull(board.grid[x][0])
        }
    }

    @Test
    fun `clearing a full column works`() {
        val board = Board(8)
        val piece = Piece.Templates.first { it.id == "1x1" }
        
        for (y in 0 until 8) {
            board.place(piece, Point(0, y))
        }
        
        val linesCleared = board.clearFullLines()
        assertEquals(1, linesCleared)
        for (y in 0 until 8) {
            assertNull(board.grid[0][y])
        }
    }
}
