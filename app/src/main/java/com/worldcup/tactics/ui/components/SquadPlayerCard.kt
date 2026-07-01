package com.worldcup.tactics.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.worldcup.tactics.domain.model.Player
import com.worldcup.tactics.domain.model.positionGroup
import com.worldcup.tactics.ui.theme.AccentGreen
import com.worldcup.tactics.ui.theme.TextPrimary
import com.worldcup.tactics.ui.theme.TextSecondary
import com.worldcup.tactics.ui.theme.positionChipColors
import com.worldcup.tactics.ui.theme.wctCard

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        modifier = modifier.padding(bottom = 12.dp),
        color = TextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 2.sp
    )
}

@Composable
fun SquadPlayerCard(
    player: Player,
    modifier: Modifier = Modifier
) {
    val chipColors = positionChipColors(player.positionGroup())

    Row(
        modifier = modifier
            .fillMaxWidth()
            .wctCard(cornerRadius = 16.dp)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = player.photoUrl,
            contentDescription = player.name,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(TextSecondary.copy(alpha = 0.2f)),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp)
        ) {
            Text(
                text = player.name,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = player.clubName ?: player.nationality,
                color = TextSecondary,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            player.number?.let { number ->
                Text(
                    text = number.toString(),
                    color = AccentGreen,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(chipColors.background)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = player.position.uppercase(),
                    color = chipColors.foreground,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
