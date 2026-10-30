package com.nramosds.khipora.presentation.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nramosds.khipora.AppContainer
import com.nramosds.khipora.domain.model.DailyClosing
import com.nramosds.khipora.domain.model.Payment
import com.nramosds.khipora.domain.model.PaymentMethod
import com.nramosds.khipora.presentation.statistics.components.AudienceDonutChartCard
import com.nramosds.khipora.presentation.statistics.components.IncomeLineChartCard
import com.nramosds.khipora.presentation.statistics.components.PaymentDistributionCard
import com.nramosds.khipora.presentation.statistics.components.StatisticsMetrics
import com.nramosds.khipora.presentation.statistics.components.TotalIncomeCard
import com.nramosds.khipora.utils.AppTheme
import com.nramosds.khipora.utils.components.AppCard
import com.nramosds.khipora.utils.viewModelFactory

@Composable
fun StatisticsScreen(businessId: Long, container: AppContainer) {
    val viewModel: StatisticsViewModel =
        viewModel(factory = viewModelFactory { StatisticsViewModel(container.closingRepository) })
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(businessId) { viewModel.load(businessId) }
    StatisticsContent(uiState, onRetry = { viewModel.load(businessId) })
}

@Composable
internal fun StatisticsContent(uiState: StatisticsUiState, onRetry: () -> Unit) {
    val summary = remember(uiState.closings, uiState.paymentMethods) {
        StatisticsSummary.calculate(uiState.closings, uiState.paymentMethods)
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { Text("Así va tu negocio", style = MaterialTheme.typography.headlineSmall) }
        when {
            uiState.isLoading -> item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            uiState.error != null -> item {
                AppCard {
                    Icon(Icons.Outlined.CloudOff, null, tint = MaterialTheme.colorScheme.error)
                    Text("No pudimos cargar tus resultados", style = MaterialTheme.typography.titleMedium)
                    Text(uiState.error)
                    TextButton(onClick = onRetry) { Text("Reintentar") }
                }
            }
            uiState.closings.isEmpty() -> item {
                AppCard {
                    Icon(Icons.Outlined.Insights, null, Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    Text("Aquí comienza tu historia", style = MaterialTheme.typography.titleLarge)
                    Text("Registra tu primer cierre para ver tus ingresos, clientes y métodos de pago.")
                }
            }
            else -> {
                item { TotalIncomeCard(summary) }
                item { StatisticsMetrics(summary) }
                item { IncomeLineChartCard(summary.dailyIncome) }
                item { AudienceDonutChartCard(uiState.closings) }
                item { PaymentDistributionCard(summary.paymentMethods) }
                // El historial completo se mantiene exclusivamente en la pantalla Historial.
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
private fun StatisticsPreview() {
    AppTheme {
        StatisticsContent(
            StatisticsUiState(
                isLoading = false,
                closings = listOf(
                    DailyClosing(
                        id = 1,
                        date = "2026-10-27",
                        units = 24,
                        customers = 15,
                        startTime = "09:00",
                        endTime = "12:00",
                        audience = "adultos",
                        notes = null,
                        payments = listOf(
                            Payment(1, "120.00".toBigDecimal()),
                            Payment(2, "80.00".toBigDecimal())
                        ),
                        consumptionReason = "calor",
                        attribute = "precio_justo"
                    ),
                    DailyClosing(
                        id = 2,
                        date = "2026-10-28",
                        units = 30,
                        customers = 18,
                        startTime = "12:00",
                        endTime = "15:00",
                        audience = "jovenes",
                        notes = null,
                        payments = listOf(Payment(1, "280.00".toBigDecimal())),
                        consumptionReason = "costumbre",
                        attribute = "atencion"
                    ),
                    DailyClosing(
                        id = 3,
                        date = "2026-10-29",
                        units = 42,
                        customers = 25,
                        startTime = "15:00",
                        endTime = "17:00",
                        audience = "adultos",
                        notes = null,
                        payments = listOf(Payment(2, "360.00".toBigDecimal())),
                        consumptionReason = "calor",
                        attribute = "limpieza"
                    )
                ),
                paymentMethods = listOf(PaymentMethod(1, "Efectivo"), PaymentMethod(2, "Yape"))
            ),
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun EmptyStatisticsPreview() {
    AppTheme { StatisticsContent(StatisticsUiState(isLoading = false), onRetry = {}) }
}
