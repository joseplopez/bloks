package com.centelles.bloks.ui.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.centelles.bloks.data.GameRepository
import com.centelles.bloks.engine.logic.GameEngine
import com.centelles.bloks.engine.model.Point
import com.centelles.bloks.monetization.AnalyticsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameEngine: GameEngine,
    private val repository: GameRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    var gameState by mutableStateOf(gameEngine.getGameState())
        private set

    var board by mutableStateOf(gameState.board)
        private set

    val highScore = repository.highScoreFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    val coins = repository.coinsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    // Estado para la previsualización del arrastre (ghost piece)
    var dragPreviewPosition by mutableStateOf<Point?>(null)
    var activeDraggingPieceIndex by mutableStateOf<Int?>(null)

    // Estado de Power-Ups
    var isBombModeActive by mutableStateOf(false)
        private set

    init {
        // Iniciar partida si es la primera vez o si la partida anterior había terminado
        if (gameState.isGameOver || (gameState.availablePieces.all { it == null } && gameState.score == 0)) {
            startNewGame()
        }
    }

    fun startNewGame() {
        gameEngine.startNewGame()
        analyticsManager.logGameStart()
        syncState()
    }

    // Estado para animaciones
    var clearingPoints by mutableStateOf<Set<Point>>(emptySet())
        private set
    
    var lastPointsGained by mutableStateOf(0)
        private set

    var showComboAnimation by mutableStateOf(false)
        private set

    data class FloatingScore(val score: Int, val position: Point, val id: Long = System.currentTimeMillis())
    var floatingScores by mutableStateOf<List<FloatingScore>>(emptyList())
        private set

    fun onPieceDropped(pieceIndex: Int, at: Point) {
        val oldBoard = board.copy()
        val success = gameEngine.placePiece(pieceIndex, at)
        if (success) {
            val newGameState = gameEngine.getGameState()
            val newBoard = newGameState.board
            
            // Detectar qué celdas se han limpiado para la animación
            val cleared = mutableSetOf<Point>()
            for (x in 0 until 8) {
                for (y in 0 until 8) {
                    if (oldBoard.grid[x][y] != null && newBoard.grid[x][y] == null) {
                        cleared.add(Point(x, y))
                    }
                }
            }
            
            val pointsGained = newGameState.score - gameState.score
            if (pointsGained > 0) {
                floatingScores = floatingScores + FloatingScore(pointsGained, at)
                viewModelScope.launch {
                    kotlinx.coroutines.delay(1000)
                    floatingScores = floatingScores.filter { it.position != at }
                }
            }

            if (cleared.isNotEmpty()) {
                clearingPoints = cleared
                showComboAnimation = newGameState.streak > 1 || cleared.size > 8
                lastPointsGained = newGameState.score - gameState.score
                
                viewModelScope.launch {
                    kotlinx.coroutines.delay(500)
                    clearingPoints = emptySet()
                    showComboAnimation = false
                }
            }

            syncState()
            if (gameState.isGameOver) {
                saveGameResult()
            }
        }
        clearDragState()
    }

    private fun saveGameResult() {
        analyticsManager.logGameOver(gameState.score, highScore.value)
        viewModelScope.launch {
            repository.saveHighScore(gameState.score)
            // Example: earn 1 coin for every 10 points
            repository.addCoins(gameState.score / 10)
        }
    }

    fun updateDragPreview(pieceIndex: Int, position: Point?) {
        activeDraggingPieceIndex = pieceIndex
        dragPreviewPosition = position
    }

    fun clearDragState() {
        activeDraggingPieceIndex = null
        dragPreviewPosition = null
    }

    fun continueGame() {
        gameEngine.continueAfterGameOver()
        syncState()
    }

    fun rotatePiece(pieceIndex: Int) {
        viewModelScope.launch {
            val success = repository.spendCoins(25)
            if (success) {
                gameEngine.rotatePiece(pieceIndex)
                syncState()
            }
        }
    }

    fun shufflePieces() {
        viewModelScope.launch {
            val success = repository.spendCoins(50)
            if (success) {
                gameEngine.shufflePieces()
                syncState()
            }
        }
    }

    fun toggleBombMode() {
        isBombModeActive = !isBombModeActive
    }

    fun useBombAt(point: Point) {
        if (!isBombModeActive) return
        viewModelScope.launch {
            val success = repository.spendCoins(100)
            if (success) {
                gameEngine.clearArea(point)
                isBombModeActive = false
                syncState()
            }
        }
    }

    private fun syncState() {
        gameState = gameEngine.getGameState()
        board = gameState.board.copy()
    }
}
