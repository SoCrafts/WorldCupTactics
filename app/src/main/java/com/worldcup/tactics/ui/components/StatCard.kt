package com.worldcup.tactics.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worldcup.tactics.ui.theme.AccentGreen
import com.worldcup.tactics.ui.theme.TextSecondary
import com.worldcup.tactics.ui.theme.wctCard

@Composable
fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .wctCard(cornerRadius = 12.dp)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = AccentGreen,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 13.sp
        )
    }
}
