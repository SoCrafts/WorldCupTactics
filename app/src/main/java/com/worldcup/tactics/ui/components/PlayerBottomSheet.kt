package com.worldcup.tactics.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.worldcup.tactics.domain.model.Player
import com.worldcup.tactics.domain.model.positionGroup
import com.worldcup.tactics.ui.theme.AccentGreen
import com.worldcup.tactics.ui.theme.BgCard
import com.worldcup.tactics.ui.theme.TextPrimary
import com.worldcup.tactics.ui.theme.TextSecondary
import com.worldcup.tactics.ui.theme.positionChipColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerBottomSheet(
    player: Player,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val chipColors = positionChipColors(player.positionGroup())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BgCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = player.photoUrl,
                contentDescription = player.name,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(TextSecondary.copy(alpha = 0.2f)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = player.name,
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                player.number?.let { number ->
                    Text(
                        text = "#$number",
                        color = AccentGreen,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = player.position,
                    color = chipColors.foreground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = player.nationality,
                color = TextSecondary,
                fontSize = 16.sp
            )
            player.clubName?.let { club ->
                Text(
                    text = club,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
