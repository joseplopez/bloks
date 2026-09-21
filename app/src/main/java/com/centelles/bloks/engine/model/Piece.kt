package com.centelles.bloks.engine.model

data class Piece(
    val id: String,
    val blocks: List<Point>,
    val color: BlockColor,
    val width: Int,
    val height: Int
) {
    fun rotated(): Piece {
        val newBlocks = blocks.map { Point(it.y, -it.x) }
        val minX = newBlocks.minOf { it.x }
        val minY = newBlocks.minOf { it.y }
        val normalizedBlocks = newBlocks.map { Point(it.x - minX, it.y - minY) }
        
        return copy(
            blocks = normalizedBlocks,
            width = height,
            height = width
        )
    }

    companion object {
        val Templates = listOf(
            // Single
            Piece("1x1", listOf(Point(0, 0)), BlockColor.BLUE, 1, 1),
            // Lines
            Piece("1x2", listOf(Point(0, 0), Point(0, 1)), BlockColor.GREEN, 1, 2),
            Piece("2x1", listOf(Point(0, 0), Point(1, 0)), BlockColor.GREEN, 2, 1),
            Piece("1x3", (0..2).map { Point(0, it) }, BlockColor.ORANGE, 1, 3),
            Piece("3x1", (0..2).map { Point(it, 0) }, BlockColor.ORANGE, 3, 1),
            Piece("1x4", (0..3).map { Point(0, it) }, BlockColor.RED, 1, 4),
            Piece("4x1", (0..3).map { Point(it, 0) }, BlockColor.RED, 4, 1),
            Piece("1x5", (0..4).map { Point(0, it) }, BlockColor.PURPLE, 1, 5),
            Piece("5x1", (0..4).map { Point(it, 0) }, BlockColor.PURPLE, 5, 1),
            // Squares
            Piece("2x2", listOf(Point(0,0), Point(1,0), Point(0,1), Point(1,1)), BlockColor.YELLOW, 2, 2),
            Piece("3x3", (0..2).flatMap { x -> (0..2).map { y -> Point(x, y) } }, BlockColor.TEAL, 3, 3),
            // L-Shapes
            Piece("L2x2", listOf(Point(0,0), Point(0,1), Point(1,1)), BlockColor.BLUE, 2, 2),
            Piece("L3x3", listOf(Point(0,0), Point(0,1), Point(0,2), Point(1,2), Point(2,2)), BlockColor.ORANGE, 3, 3)
        )
    }
}
