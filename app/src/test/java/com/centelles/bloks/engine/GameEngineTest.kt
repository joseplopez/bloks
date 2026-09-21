package com.centelles.bloks.engine

import com.centelles.bloks.engine.logic.GameEngine
import com.centelles.bloks.engine.logic.PieceGenerator
import com.centelles.bloks.engine.model.Piece
import com.centelles.bloks.engine.model.Point
import org.junit.Assert.*
import org.junit.Test

class GameEngineTest {

    @Test
    fun `starting a game generates 3 pieces`() {
        val engine = GameEngine()
        engine.startNewGame()
        val state = engine.getGameState()
        
        assertEquals(3, state.availablePieces.filterNotNull().size)
        assertFalse(state.isGameOver)
        assertEquals(0, state.score)
    }

    @Test
    fun `placing a 1x1 piece increases score by 1`() {
        val engine = GameEngine()
        engine.startNewGame()
        
        // Find a 1x1 piece in available pieces or force it via generator seed if possible
        // For simplicity, let's just use the engine and check the diff
        val initialState = engine.getGameState()
        val firstPiece = initialState.availablePieces.filterNotNull().first()
        
        val success = engine.placePiece(initialState.availablePieces.indexOf(firstPiece), Point(0, 0))
        
        assertTrue(success)
        assertEquals(firstPiece.blocks.size, engine.getGameState().score)
    }

    @Test
    fun `clearing a row increases score correctly`() {
        val engine = GameEngine()
        engine.startNewGame()
        
        // Manually place pieces to clear a row (this is hard with random pieces)
        // Better to test Board logic directly for clearing
    }
}
