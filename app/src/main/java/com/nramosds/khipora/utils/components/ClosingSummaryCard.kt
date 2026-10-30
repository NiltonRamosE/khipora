package com.nramosds.khipora.utils.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nramosds.khipora.domain.model.DailyClosing
import com.nramosds.khipora.domain.model.Payment
import com.nramosds.khipora.utils.AppTheme
import com.nramosds.khipora.utils.formatAudience
import com.nramosds.khipora.utils.formatCurrency
import com.nramosds.khipora.utils.formatDate
import com.nramosds.khipora.utils.formatTime

@Composable
internal fun ClosingSummaryCard(closing: DailyClosing, showDetails: Boolean = false) {
    AppCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.EventAvailable, null, tint = MaterialTheme.colorScheme.primary)
            Text(
                formatDate(closing.date, short = !showDetails),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            formatCurrency(closing.total),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "${closing.customers} clientes · ${closing.units} unidades",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (showDetails) {
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            Text(
                "Público: ${formatAudience(closing.audience)}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (closing.startTime != null && closing.endTime != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Schedule,
                        null,
                        Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${formatTime(closing.startTime)} – ${formatTime(closing.endTime)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun ClosingSummaryPreview() {
    AppTheme {
        Box(Modifier.padding(18.dp)) {
            ClosingSummaryCard(
                listOf(
                    DailyClosing(
                        id = 1,
                        date = "2026-10-29",
                        units = 24,
                        customers = 15,
                        startTime = "09:00",
                        endTime = "18:00",
                        audience = "adultos",
                        notes = null,
                        payments = listOf(
                            Payment(1, "120.00".toBigDecimal()),
                            Payment(2, "80.00".toBigDecimal())
                        )
                    )
                ).first(), showDetails = true
            )
        }
    }
}
