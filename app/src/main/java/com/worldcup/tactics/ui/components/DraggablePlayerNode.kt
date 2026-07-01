package com.worldcup.tactics.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.worldcup.tactics.domain.model.FormationPlayer
import com.worldcup.tactics.ui.shortName
import com.worldcup.tactics.ui.theme.AccentGreen
import com.worldcup.tactics.ui.theme.BgCard
import com.worldcup.tactics.ui.theme.TextPrimary
@Composable fun NameLabel(name: String, modifier: Modifier = Modifier) { 
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text( text = name, fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold, style = TextStyle( drawStyle = Stroke(width = 2f) ) ) 
        Text( text = name, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold ) 
    } 
}

@Composable
fun DraggablePlayerNode(
    formationPlayer: FormationPlayer,
    isSelected: Boolean,
    fieldWidthPx: Float,
    fieldHeightPx: Float,
    onTap: () -> Unit,
    onDrag: (xNorm: Float, yNorm: Float) -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val nodeSize = 52.dp
    val player = formationPlayer.player

    // Position the circle centre at (xNorm·W, yNorm·H); label grows downward
    val targetXDp =
        with(density) { (formationPlayer.xNorm * fieldWidthPx).toDp() } - nodeSize / 2

    val targetYDp =
        with(density) { (formationPlayer.yNorm * fieldHeightPx).toDp() } - nodeSize / 2

    var isDragging by remember { mutableStateOf(false) }

    val xDp by animateDpAsState(
        targetValue = targetXDp,
        animationSpec = if (isDragging) snap() else tween(300),
        label = "xOffset"
    )

    val yDp by animateDpAsState(
        targetValue = targetYDp,
        animationSpec = if (isDragging) snap() else tween(300),
        label = "yOffset"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(200),
        label = "glow"
    )

    Box(
        modifier = modifier
            .offset(x = xDp, y = yDp)
            .size(nodeSize)
            // TAP
            .pointerInput(player.id) {
                detectTapGestures {
                    if (!isDragging) {
                        onTap()
                    }
                }
            }

            // DRAG
            .pointerInput(player.id, fieldWidthPx, fieldHeightPx) {

                var accumulated = Offset.Zero

                detectDragGestures(
                    onDragStart = {
                        accumulated = Offset.Zero
                        isDragging = true
                        onDragStart()
                    },
                    onDragEnd = {
                        isDragging = false
                        onDragEnd()
                    },
                    onDragCancel = {
                        isDragging = false
                    }
                ) { _, dragAmount ->

                    accumulated += dragAmount

                    val newX =
                        (formationPlayer.xNorm + accumulated.x / fieldWidthPx)
                            .coerceIn(0.05f, 0.95f)

                    val newY =
                        (formationPlayer.yNorm + accumulated.y / fieldHeightPx)
                            .coerceIn(0.05f, 0.95f)

                    onDrag(newX, newY)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Circle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = if (isSelected) 12.dp else 4.dp,
                    shape = CircleShape,
                    ambientColor = AccentGreen.copy(alpha = glowAlpha),
                    spotColor = AccentGreen.copy(alpha = glowAlpha)
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            BgCard.copy(alpha = 0.95f),
                            BgCard
                        )
                    )
                )
                .border(
                    width = if (isSelected || isDragging) 3.dp else 2.dp,
                    color = AccentGreen,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (player.photoUrl != null) {
                AsyncImage(
                    model = player.photoUrl,
                    contentDescription = player.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = player.number?.toString() ?: player.name.take(2).uppercase(),
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        NameLabel(
            name = shortName(player.name),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                //.offset(y = (+2).dp) 
        )
    }
}
