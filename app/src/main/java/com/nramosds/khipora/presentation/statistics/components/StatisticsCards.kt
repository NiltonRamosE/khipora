package com.nramosds.khipora.presentation.statistics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nramosds.khipora.domain.model.DailyClosing
import com.nramosds.khipora.domain.model.Payment
import com.nramosds.khipora.domain.model.PaymentMethod
import com.nramosds.khipora.presentation.statistics.StatisticsSummary
import com.nramosds.khipora.utils.AppTheme
import com.nramosds.khipora.utils.components.AppCard
import com.nramosds.khipora.utils.formatCurrency
import com.nramosds.khipora.utils.formatDate

@Composable
internal fun TotalIncomeCard(summary: StatisticsSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.AccountBalanceWallet, null)
                Text("Ingresos acumulados", style = MaterialTheme.typography.titleMedium)
            }
            Text(formatCurrency(summary.total), style = MaterialTheme.typography.headlineMedium)
            Text("${summary.days} días registrados", style = MaterialTheme.typography.labelLarge)
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f))
            summary.dailyIncome.lastOrNull()?.let { firstDay ->
                Text(
                    "${
                        formatDate(
                            firstDay.date,
                            true
                        )
                    } — ${formatDate(summary.dailyIncome.first().date, true)}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
internal fun StatisticsMetrics(summary: StatisticsSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                "Clientes",
                summary.customers.toString(),
                Icons.Outlined.PeopleOutline,
                Modifier.weight(1f)
            )
            MetricCard(
                "Unidades",
                summary.units.toString(),
                Icons.Outlined.Inventory2,
                Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                "Por cliente",
                formatCurrency(summary.averagePerCustomer),
                Icons.Outlined.ReceiptLong,
                Modifier.weight(1f)
            )
            MetricCard(
                "Por día",
                formatCurrency(summary.dailyAverage),
                Icons.Outlined.CalendarMonth,
                Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, icon: ImageVector, modifier: Modifier) {
    AppCard(modifier) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(
            title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun StatisticsSummaryPreview() {
    AppTheme {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val summary = StatisticsSummary.calculate(
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
                ), listOf(PaymentMethod(1, "Efectivo"), PaymentMethod(2, "Yape"))
            )
            TotalIncomeCard(summary)
            StatisticsMetrics(summary)
        }
    }
}
