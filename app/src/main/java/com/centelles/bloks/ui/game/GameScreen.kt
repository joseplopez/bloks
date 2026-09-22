package com.centelles.bloks.ui.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.centelles.bloks.R
import com.centelles.bloks.engine.model.BlockColor
import com.centelles.bloks.engine.model.Board
import com.centelles.bloks.engine.model.Piece
import com.centelles.bloks.engine.model.Point
import com.centelles.bloks.ui.components.BannerAd
import com.centelles.bloks.ui.theme.SurfaceColor
import com.centelles.bloks.ui.theme.toComposeColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    onGameOver: (Int) -> Unit
) {
    val gameState = viewModel.gameState
    val board = viewModel.board
    val highScore by viewModel.highScore.collectAsState()
    val coins by viewModel.coins.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
    
    var boardCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val haptic = LocalHapticFeedback.current

    // Animación de sacudida (shake) cuando se limpian líneas
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(viewModel.clearingPoints) {
        if (viewModel.clearingPoints.isNotEmpty()) {
            repeat(4) {
                shakeOffset.animateTo(10f, animationSpec = tween(50))
                shakeOffset.animateTo(-10f, animationSpec = tween(50))
            }
            shakeOffset.animateTo(0f)
        }
    }

    LaunchedEffect(gameState.isGameOver) {
        if (gameState.isGameOver) {
            onGameOver(gameState.score)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GameHeader(
                score = gameState.score,
                streak = gameState.streak,
                highScore = highScore,
                coins = coins
            )

            Spacer(modifier = Modifier.weight(1f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = shakeOffset.value
                    }
                    .onGloballyPositioned { boardCoordinates = it }
            ) {
                BoardView(
                    board = board,
                    previewPiece = if (viewModel.dragPreviewPosition != null) {
                        gameState.availablePieces.getOrNull(viewModel.activeDraggingPieceIndex ?: -1)
                    } else null,
                    previewPosition = viewModel.dragPreviewPosition,
                    isBombModeActive = viewModel.isBombModeActive,
                    onCellClick = { point -> viewModel.useBombAt(point) },
                    clearingPoints = viewModel.clearingPoints
                )

                // Texto de COMBO animado
                androidx.compose.animation.AnimatedVisibility(
                    visible = viewModel.showComboAnimation,
                    enter = scaleIn(initialScale = 0.5f) + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    ComboOverlay(streak = gameState.streak)
                }

                // Puntuaciones flotantes
                viewModel.floatingScores.forEach { floating ->
                    FloatingScoreView(floating, boardCoordinates)
                }
            }
// ...

            Spacer(modifier = Modifier.weight(1f))

            if (viewModel.isBombModeActive) {
                Text(
                    text = stringResource(R.string.game_bomb_mode_active),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { viewModel.shufflePieces() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    enabled = coins >= 50
                ) {
                    Text(stringResource(R.string.game_shuffle_button))
                }
                
                Button(
                    onClick = { viewModel.toggleBombMode() },
                    enabled = coins >= 100 || viewModel.isBombModeActive,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.isBombModeActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(if (viewModel.isBombModeActive) stringResource(R.string.game_cancel) else stringResource(R.string.game_bomb_button))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            PieceSelector(
                pieces = gameState.availablePieces,
                onPieceDropped = { index, point -> 
                    viewModel.onPieceDropped(index, point)
                    if (vibrationEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                },
                onDragUpdate = { index, point -> viewModel.updateDragPreview(index, point) },
                onDragCancel = { viewModel.clearDragState() },
                onRotateClick = { index -> viewModel.rotatePiece(index) },
                canRotate = coins >= 25,
                boardCoordinates = boardCoordinates
            )

            Spacer(modifier = Modifier.height(72.dp))
        }

        BannerAd(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 8.dp)
        )
    }
}

@Composable
fun GameHeader(score: Int, streak: Int, highScore: Int, coins: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.game_high_score_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = highScore.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
            
            Text(
                text = stringResource(R.string.common_coins_format, coins),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 64.sp,
            color = Color.White
        )
        Text(
            text = stringResource(R.string.game_streak_format, if (streak > 0) streak else 1),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.alpha(if (streak > 0) 1f else 0f)
        )
    }
}

@Composable
fun BoardView(
    board: Board,
    previewPiece: Piece?,
    previewPosition: Point?,
    isBombModeActive: Boolean,
    onCellClick: (Point) -> Unit,
    clearingPoints: Set<Point> = emptySet()
) {
    val cellSize = 40.dp
    val spacing = 2.dp
    val boardSizeDp = (cellSize * board.size) + (spacing * (board.size - 1))

    // Animación para los bloques que desaparecen
    val clearProgress by animateFloatAsState(
        targetValue = if (clearingPoints.isEmpty()) 0f else 1f,
        animationSpec = tween(500),
        label = "clearProgress"
    )

    Canvas(
        modifier = Modifier
            .size(boardSizeDp)
            .pointerInput(isBombModeActive) {
                if (isBombModeActive) {
                    detectTapGestures { offset ->
                        val cellSizePx = cellSize.toPx() + spacing.toPx()
                        val gridX = (offset.x / cellSizePx).toInt().coerceIn(0, board.size - 1)
                        val gridY = (offset.y / cellSizePx).toInt().coerceIn(0, board.size - 1)
                        onCellClick(Point(gridX, gridY))
                    }
                }
            }
    ) {
        val cellSizePx = cellSize.toPx()
        val spacingPx = spacing.toPx()

        // Dibujar fondo de la cuadrícula
        for (x in 0 until board.size) {
            for (y in 0 until board.size) {
                val offset = Offset(
                    x * (cellSizePx + spacingPx),
                    y * (cellSizePx + spacingPx)
                )
                drawBlock(offset, cellSizePx, SurfaceColor)
            }
        }

        // Dibujar bloques existentes
        for (x in 0 until board.size) {
            for (y in 0 until board.size) {
                val offset = Offset(
                    x * (cellSizePx + spacingPx),
                    y * (cellSizePx + spacingPx)
                )
                board.grid[x][y]?.let { color ->
                    drawBlock(offset, cellSizePx, color.toComposeColor())
                }
            }
        }

        // Dibujar bloques en proceso de limpieza (animación Block Blast)
        if (clearingPoints.isNotEmpty()) {
            for (point in clearingPoints) {
                val offset = Offset(
                    point.x * (cellSizePx + spacingPx),
                    point.y * (cellSizePx + spacingPx)
                )
                
                // Efecto: se vuelven blancos y se encogen
                val animScale = 1f - clearProgress
                val animAlpha = 1f - (clearProgress * 0.5f)
                val animColor = Color.White.copy(alpha = animAlpha)
                
                val scaledSize = cellSizePx * animScale
                val centeredOffset = offset + Offset((cellSizePx - scaledSize)/2, (cellSizePx - scaledSize)/2)
                
                drawRoundRect(
                    color = animColor,
                    topLeft = centeredOffset,
                    size = Size(scaledSize, scaledSize),
                    cornerRadius = CornerRadius(4.dp.toPx() * animScale)
                )
                
                // Brillo exterior (glow)
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.3f * (1f - clearProgress)),
                    topLeft = centeredOffset - Offset(4.dp.toPx(), 4.dp.toPx()),
                    size = Size(scaledSize + 8.dp.toPx(), scaledSize + 8.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        if (previewPiece != null && previewPosition != null) {
            for (block in previewPiece.blocks) {
                val targetX = previewPosition.x + block.x
                val targetY = previewPosition.y + block.y
                
                if (targetX in 0 until board.size && targetY in 0 until board.size) {
                    val offset = Offset(
                        targetX * (cellSizePx + spacingPx),
                        targetY * (cellSizePx + spacingPx)
                    )
                    drawBlock(offset, cellSizePx, previewPiece.color.toComposeColor().copy(alpha = 0.4f))
                }
            }
        }
    }
}

@Composable
fun FloatingScoreView(floating: GameViewModel.FloatingScore, boardCoordinates: LayoutCoordinates?) {
    val yOffset = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    
    LaunchedEffect(Unit) {
        launch {
            yOffset.animateTo(-100f, animationSpec = tween(1000, easing = LinearOutSlowInEasing))
        }
        launch {
            delay(500)
            alpha.animateTo(0f, animationSpec = tween(500))
        }
    }

    val density = LocalDensity.current
    val cellSize = 40.dp
    val spacing = 2.dp
    val cellSizePx = with(density) { (cellSize + spacing).toPx() }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (floating.position.x * cellSizePx - (cellSizePx * 3.5f)).roundToInt(),
                    (floating.position.y * cellSizePx - (cellSizePx * 3.5f) + yOffset.value).roundToInt()
                )
            }
            .alpha(alpha.value)
    ) {
        Text(
            text = "+${floating.score}",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color.Yellow,
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black,
                    offset = Offset(2f, 2f),
                    blurRadius = 4f
                )
            )
        )
    }
}
@Composable
fun ComboOverlay(streak: Int) {
    val text = when {
        streak > 5 -> stringResource(R.string.game_combo_incredible)
        streak > 3 -> stringResource(R.string.game_combo_super)
        else -> stringResource(R.string.game_combo)
    }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = text,
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Black,
                brush = Brush.linearGradient(
                    colors = listOf(Color.Cyan, Color.Magenta, Color.Yellow)
                )
            ),
            modifier = Modifier.graphicsLayer {
                rotationZ = -5f
            }
        )
        if (streak > 1) {
            Text(
                text = "x$streak",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun DrawScope.drawBlock(offset: Offset, size: Float, color: Color) {
    drawRoundRect(
        color = color,
        topLeft = offset,
        size = Size(size, size),
        cornerRadius = CornerRadius(4.dp.toPx())
    )
}

@Composable
fun PieceSelector(
    pieces: List<Piece?>,
    onPieceDropped: (Int, Point) -> Unit,
    onDragUpdate: (Int, Point?) -> Unit,
    onDragCancel: () -> Unit,
    onRotateClick: (Int) -> Unit,
    canRotate: Boolean,
    boardCoordinates: LayoutCoordinates?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        pieces.forEachIndexed { index, piece ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                    if (piece != null) {
                        DraggablePiece(
                            piece = piece,
                            onDropped = { point -> onPieceDropped(index, point) },
                            onDragUpdate = { point -> onDragUpdate(index, point) },
                            onDragCancel = onDragCancel,
                            boardCoordinates = boardCoordinates
                        )
                    }
                }
                if (piece != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { onRotateClick(index) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        enabled = canRotate
                    ) {
                        Text(stringResource(R.string.game_rotate_cost), fontSize = 12.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }
}

@Composable
fun DraggablePiece(
    piece: Piece,
    onDropped: (Point) -> Unit,
    onDragUpdate: (Point?) -> Unit,
    onDragCancel: () -> Unit,
    boardCoordinates: LayoutCoordinates?
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    var lastGridPos by remember { mutableStateOf<Point?>(null) }
    
    val density = LocalDensity.current
    val maxDim = maxOf(piece.width, piece.height)
    val cellSize = if (maxDim > 0) (65.dp / maxDim).coerceAtMost(22.dp) else 22.dp
    val boardCellSize = 40.dp
    val boardSpacing = 2.dp

    val scale by animateFloatAsState(if (isDragging) 1.2f else 1.0f, label = "pieceScale")

    Box(
        modifier = Modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .scale(scale)
            .onGloballyPositioned { coords ->
                if (isDragging && boardCoordinates != null) {
                    val piecePos = coords.positionInRoot()
                    val boardPos = boardCoordinates.positionInRoot()
                    
                    val relativeX = piecePos.x - boardPos.x
                    val relativeY = piecePos.y - boardPos.y
                    
                    val cellSizePx = with(density) { boardCellSize.toPx() + boardSpacing.toPx() }
                    
                    val gridX = (relativeX / cellSizePx).roundToInt()
                    val gridY = (relativeY / cellSizePx).roundToInt()
                    
                    lastGridPos = Point(gridX, gridY)
                    onDragUpdate(lastGridPos)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        lastGridPos?.let { onDropped(it) } ?: onDragCancel()
                        offset = Offset.Zero
                        isDragging = false
                        lastGridPos = null
                    },
                    onDragCancel = {
                        offset = Offset.Zero
                        isDragging = false
                        lastGridPos = null
                        onDragCancel()
                    },
                    onDrag = { change, dragAmount ->
                        offset += dragAmount
                        change.consume()
                    }
                )
            }
    ) {
        PieceView(piece = piece, cellSize = if (isDragging) boardCellSize else cellSize)
    }
}

@Composable
fun PieceView(piece: Piece, cellSize: Dp) {
    val spacing = 1.dp
    val width = (cellSize * piece.width) + (spacing * (piece.width - 1))
    val height = (cellSize * piece.height) + (spacing * (piece.height - 1))

    Canvas(modifier = Modifier.size(width, height)) {
        val cellSizePx = cellSize.toPx()
        val spacingPx = spacing.toPx()

        for (block in piece.blocks) {
            val offset = Offset(
                block.x * (cellSizePx + spacingPx),
                block.y * (cellSizePx + spacingPx)
            )
            drawBlock(offset, cellSizePx, piece.color.toComposeColor())
        }
    }
}
