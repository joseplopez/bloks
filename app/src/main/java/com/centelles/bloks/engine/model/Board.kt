package com.centelles.bloks.engine.model

class Board(val size: Int = 8) {
    val grid: Array<Array<BlockColor?>> = Array(size) { arrayOfNulls<BlockColor>(size) }

    fun canPlace(piece: Piece, at: Point): Boolean {
        for (block in piece.blocks) {
            val targetX = at.x + block.x
            val targetY = at.y + block.y
            
            if (targetX !in 0 until size || targetY !in 0 until size) return false
            if (grid[targetX][targetY] != null) return false
        }
        return true
    }

    fun place(piece: Piece, at: Point) {
        for (block in piece.blocks) {
            grid[at.x + block.x][at.y + block.y] = piece.color
        }
    }

    fun clearFullLines(): Int {
        val rowsToClear = mutableListOf<Int>()
        val colsToClear = mutableListOf<Int>()

        for (y in 0 until size) {
            if ((0 until size).all { x -> grid[x][y] != null }) rowsToClear.add(y)
        }

        for (x in 0 until size) {
            if ((0 until size).all { y -> grid[x][y] != null }) colsToClear.add(x)
        }

        for (y in rowsToClear) {
            for (x in 0 until size) grid[x][y] = null
        }
        for (x in colsToClear) {
            for (y in 0 until size) grid[x][y] = null
        }

        return rowsToClear.size + colsToClear.size
    }

    fun hasAnyPossibleMove(availablePieces: List<Piece?>): Boolean {
        return availablePieces.filterNotNull().any { piece ->
            (0 until size).any { x ->
                (0 until size).any { y ->
                    canPlace(piece, Point(x, y))
                }
            }
        }
    }
    
    fun copy(): Board {
        val newBoard = Board(size)
        for (x in 0 until size) {
            for (y in 0 until size) {
                newBoard.grid[x][y] = this.grid[x][y]
            }
        }
        return newBoard
    }
}
