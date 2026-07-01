package com.worldcup.tactics.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.worldcup.tactics.domain.model.PositionGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border

private val WctDarkColorScheme = darkColorScheme(
    primary = AccentGreen,
    onPrimary = BgPrimary,
    secondary = PositionGk,
    background = BgPrimary,
    surface = BgCard,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = BorderColor
)

@Composable
fun WorldCupTacticsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WctDarkColorScheme,
        content = content
    )
}

fun Modifier.wctCard(cornerRadius: Dp = 12.dp): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(BgCard)
    .border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(cornerRadius))

data class PositionChipColors(
    val foreground: Color,
    val background: Color
)

fun positionChipColors(group: PositionGroup): PositionChipColors = when (group) {
    PositionGroup.GK -> PositionChipColors(PositionGk, PositionGk.copy(alpha = 0.15f))
    PositionGroup.DEF -> PositionChipColors(PositionDef, PositionDef.copy(alpha = 0.15f))
    PositionGroup.MID -> PositionChipColors(PositionMid, PositionMid.copy(alpha = 0.15f))
    PositionGroup.FWD -> PositionChipColors(PositionFwd, PositionFwd.copy(alpha = 0.15f))
    PositionGroup.ALL -> PositionChipColors(TextSecondary, TextSecondary.copy(alpha = 0.15f))
}
