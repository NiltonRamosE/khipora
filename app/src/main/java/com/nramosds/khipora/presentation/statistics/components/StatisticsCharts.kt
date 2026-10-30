package com.nramosds.khipora.presentation.statistics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nramosds.khipora.domain.model.DailyClosing
import com.nramosds.khipora.domain.model.Payment
import com.nramosds.khipora.domain.model.PaymentMethod
import com.nramosds.khipora.presentation.statistics.DailyIncome
import com.nramosds.khipora.presentation.statistics.PaymentMethodIncome
import com.nramosds.khipora.presentation.statistics.StatisticsSummary
import com.nramosds.khipora.utils.AppTheme
import com.nramosds.khipora.utils.components.AppCard
import com.nramosds.khipora.utils.formatCurrency
import com.nramosds.khipora.utils.formatDate
import java.math.RoundingMode
import kotlin.math.roundToInt

@Composable
internal fun PaymentDistributionCard(paymentMethods: List<PaymentMethodIncome>) {
    AppCard {
        Icon(Icons.Outlined.Payments, null, tint = MaterialTheme.colorScheme.primary)
        Text("¿Cómo te pagan?", style = MaterialTheme.typography.titleLarge)
        paymentMethods.forEach { paymentMethod ->
            Column(
                Modifier.padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        paymentMethod.name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        "${(paymentMethod.fraction * 100).roundToInt()} %",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                LinearProgressIndicator(
                    progress = { paymentMethod.fraction },
                    modifier = Modifier.fillMaxWidth().height(8.dp)
                )
                Text(
                    formatCurrency(paymentMethod.amount),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        if (paymentMethods.isEmpty()) Text("No hay pagos registrados.")
    }
}

@Composable
internal fun IncomeTrendCard(days: List<DailyIncome>) {
    val maximum = days.maxOfOrNull { it.amount }
    AppCard {
        Icon(Icons.Outlined.Insights, null, tint = MaterialTheme.colorScheme.primary)
        Text("Tu actividad reciente", style = MaterialTheme.typography.titleLarge)
        Text(
            "Últimos ${days.size} días con registro",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        days.asReversed().forEach { day ->
            val fraction = if (maximum == null || maximum.signum() == 0) 0f
            else day.amount.divide(maximum, 4, RoundingMode.HALF_UP).toFloat().coerceIn(0f, 1f)
            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        formatDate(day.date, true),
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(formatCurrency(day.amount), style = MaterialTheme.typography.labelLarge)
                }
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.fillMaxWidth().height(8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun StatisticsChartsPreview() {
    AppTheme {
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
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            IncomeTrendCard(summary.dailyIncome.take(7))
            PaymentDistributionCard(summary.paymentMethods)
        }
    }
}
