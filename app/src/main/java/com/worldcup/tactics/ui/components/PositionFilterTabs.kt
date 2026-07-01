package com.worldcup.tactics.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worldcup.tactics.domain.model.PositionGroup
import com.worldcup.tactics.ui.theme.AccentGreen
import com.worldcup.tactics.ui.theme.BgCard
import com.worldcup.tactics.ui.theme.BgPrimary
import com.worldcup.tactics.ui.theme.BorderColor
import com.worldcup.tactics.ui.theme.TextSecondary

@Composable
fun PositionFilterTabs(
    selected: PositionGroup,
    onSelected: (PositionGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        PositionGroup.ALL to "All",
        PositionGroup.GK to "GK",
        PositionGroup.DEF to "DEF",
        PositionGroup.MID to "MID",
        PositionGroup.FWD to "FWD"
    )

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { (group, label) ->
            val isSelected = selected == group
            Surface(
                onClick = { onSelected(group) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) AccentGreen else BgCard,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) AccentGreen else BorderColor
                )
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    color = if (isSelected) BgPrimary else TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
