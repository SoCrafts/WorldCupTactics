package com.worldcup.tactics.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.worldcup.tactics.domain.model.FormationPlayer
import com.worldcup.tactics.ui.theme.PitchGreen
import com.worldcup.tactics.ui.theme.PitchGreenDark

@Composable
fun FootballField(
    players: List<FormationPlayer>,
    selectedPlayerId: Int?,
    onPlayerTap: (Int) -> Unit,
    onClearSelection: () -> Unit,
    onPlayerDrag: (Int, Float, Float) -> Unit,
    onPlayerDragStart: (Int) -> Unit,
    onPlayerDragEnd: (Int, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val fieldWidthPx = with(density) { maxWidth.toPx() }
        val fieldHeightPx = with(density) { maxHeight.toPx() }

        // Background tap clears selection
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { onClearSelection() }
                }
        ) {
            drawRoundRect(
                color = PitchGreen,
                size = size,
                cornerRadius = CornerRadius(16f, 16f)
            )

            val left = 16f
            val top = 16f
            val w = size.width - 32f
            val h = size.height - 32f

            val strokeWidth = 3f
            val lineColor = Color.White.copy(alpha = 0.3f)
            val stroke = Stroke(width = strokeWidth)

            // Bordo campo
            drawRoundRect(
                lineColor,
                Offset(left, top),
                Size(w, h),
                cornerRadius = CornerRadius(8f, 8f),
                style = stroke
            )

            // Linea di metà campo ORIZZONTALE (da sinistra a destra al centro dell'altezza)
            val centerY = top + h / 2f
            drawLine(
                color = lineColor,
                start = Offset(left, centerY),
                end = Offset(left + w, centerY),
                strokeWidth = strokeWidth
            )

            // Cerchio centrale
            val midX = left + w / 2f
            drawCircle(lineColor, radius = w * 0.12f, center = Offset(midX, centerY), style = stroke)
            drawCircle(lineColor, radius = 6f, center = Offset(midX, centerY))

            // Aree di rigore
            val boxW = w * 0.55f
            val boxH = h * 0.18f
            drawRect(lineColor, Offset(left + (w - boxW) / 2f, top), Size(boxW, boxH), style = stroke)
            drawRect(
                lineColor,
                Offset(left + (w - boxW) / 2f, top + h - boxH),
                Size(boxW, boxH),
                style = stroke
            )
        }

        players.forEach { formationPlayer ->
            DraggablePlayerNode(
                formationPlayer = formationPlayer,
                isSelected = formationPlayer.player.id == selectedPlayerId,
                modifier = Modifier.size(52.dp),
                fieldWidthPx = fieldWidthPx,
                fieldHeightPx = fieldHeightPx,
                onTap = { onPlayerTap(formationPlayer.player.id) },
                onDrag = { xNorm, yNorm ->
                    onPlayerDrag(formationPlayer.player.id, xNorm, yNorm)
                },
                onDragStart = {
                    onPlayerDragStart(formationPlayer.player.id)
                },
                onDragEnd = {
                    onPlayerDragEnd(formationPlayer.player.id, fieldWidthPx, fieldHeightPx)
                }
            )
        }
    }
}

