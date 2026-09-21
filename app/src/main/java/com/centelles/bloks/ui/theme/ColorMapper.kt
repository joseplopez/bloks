package com.centelles.bloks.ui.theme

import androidx.compose.ui.graphics.Color
import com.centelles.bloks.engine.model.BlockColor

fun BlockColor.toComposeColor(): Color {
    return when (this) {
        BlockColor.BLUE -> BlockBlue
        BlockColor.GREEN -> BlockGreen
        BlockColor.ORANGE -> BlockOrange
        BlockColor.RED -> BlockRed
        BlockColor.PURPLE -> BlockPurple
        BlockColor.YELLOW -> BlockYellow
        BlockColor.TEAL -> BlockTeal
    }
}
