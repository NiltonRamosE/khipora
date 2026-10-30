package com.nramosds.khipora.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nramosds.khipora.AppContainer
import com.nramosds.khipora.R
import com.nramosds.khipora.domain.model.DailyClosing
import com.nramosds.khipora.domain.model.Payment
import com.nramosds.khipora.utils.AppTheme
import com.nramosds.khipora.utils.components.AppCard
import com.nramosds.khipora.utils.formatCurrency
import com.nramosds.khipora.utils.viewModelFactory

@Composable
fun HomeScreen(
    businessId: Long,
    container: AppContainer,
    onNewClosing: () -> Unit
) {
    val viewModel: HomeViewModel =
        viewModel(factory = viewModelFactory { HomeViewModel(container.closingRepository) })
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(businessId) { viewModel.load(businessId) }

    HomeContent(
        uiState = uiState,
        onNewClosing = onNewClosing,
        onRetry = { viewModel.load(businessId) }
    )
}

@Composable
internal fun HomeContent(
    uiState: HomeUiState,
    onNewClosing: () -> Unit,
    onRetry: () -> Unit
) {
    // Guardar el nombre (no el enum) facilita restaurar el modal tras rotaciones.
    var selectedMetricName by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedMetric = HomeMetric.entries.firstOrNull { it.name == selectedMetricName }

    if (selectedMetric != null) {
        HomeDetailSheet(
            metric = selectedMetric,
            uiState = uiState,
            onRefresh = onRetry,
            onDismiss = { selectedMetricName = null }
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val viewportHeight = maxHeight
        val density = LocalDensity.current
        var summaryHeightPx by remember { mutableIntStateOf(0) }
        val summaryHeight = with(density) { summaryHeightPx.toDp() }
        // Restamos el padding exterior (18dp arriba y abajo) para que la imagen
        // se centre dentro de la altura que queda realmente libre.
        val imageAreaHeight =
            (viewportHeight - 36.dp - summaryHeight).coerceAtLeast(180.dp)
        val imageSize = (viewportHeight * 0.26f).coerceIn(128.dp, 230.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { summaryHeightPx = it.height },
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Text("Resumen", style = MaterialTheme.typography.headlineSmall)

                if (uiState.isLoading) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }

                uiState.error?.let { message ->
                    AppCard {
                        Text(message, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = onRetry) { Text("Reintentar") }
                    }
                }

                if (!uiState.isLoading && uiState.error == null) {
                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMetricName = HomeMetric.INCOME.name }
                    ) {
                        Text(
                            "Ingresos acumulados",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            formatCurrency(uiState.income),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "${uiState.closings.size} días registrados",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Ver ingresos por fecha",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedMetricName = HomeMetric.CUSTOMERS.name }
                        ) {
                            Icon(
                                Icons.Outlined.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text("Clientes", style = MaterialTheme.typography.labelLarge)
                            Text("${uiState.customers}", style = MaterialTheme.typography.headlineSmall)
                            Text(
                                "Ver por fecha",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        AppCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedMetricName = HomeMetric.UNITS.name }
                        ) {
                            Icon(
                                Icons.Outlined.Inventory2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text("Unidades", style = MaterialTheme.typography.labelLarge)
                            Text("${uiState.units}", style = MaterialTheme.typography.headlineSmall)
                            Text(
                                "Ver por fecha",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Button(
                        onClick = onNewClosing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Registrar cierre")
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().height(imageAreaHeight),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.coco),
                    contentDescription = null, // imagen decorativa
                    modifier = Modifier.size(imageSize),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 700)
@Composable
private fun HomeSmallPreview() {
    AppTheme {
        HomeContent(
            HomeUiState(
                isLoading = false,
                closings = listOf(
                    DailyClosing(
                        id = 1,
                        date = "2026-10-29",
                        units = 24,
                        customers = 15,
                        startTime = "09:00",
                        endTime = "12:00",
                        audience = "adultos",
                        notes = null,
                        payments = listOf(
                            Payment(1, "120.00".toBigDecimal()),
                            Payment(2, "80.00".toBigDecimal())
                        )
                    )
                )
            ),
            onNewClosing = {},
            onRetry = {}
        )
    }
}
