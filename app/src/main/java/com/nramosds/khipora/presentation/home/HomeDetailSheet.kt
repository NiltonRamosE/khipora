package com.nramosds.khipora.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nramosds.khipora.domain.model.DailyClosing
import com.nramosds.khipora.utils.formatCurrency
import com.nramosds.khipora.utils.formatDate
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class HomeMetric(val title: String) {
    INCOME("Ingresos por fecha"),
    CUSTOMERS("Clientes por fecha"),
    UNITS("Unidades por fecha")
}

private enum class DateBound { FROM, TO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeDetailSheet(
    metric: HomeMetric,
    uiState: HomeUiState,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit
) {
    var fromDate by rememberSaveable(metric.name) { mutableStateOf<String?>(null) }
    var toDate by rememberSaveable(metric.name) { mutableStateOf<String?>(null) }
    var editingBound by remember { mutableStateOf<DateBound?>(null) }

    val filtered = remember(uiState.closings, fromDate, toDate) {
        uiState.closings
            .asSequence()
            .filter { closing ->
                (fromDate == null || closing.date >= fromDate!!) &&
                    (toDate == null || closing.date <= toDate!!)
            }
            .sortedByDescending { it.date }
            .toList()
    }

    editingBound?.let { bound ->
        HomeFilterDateDialog(
            initialDate = if (bound == DateBound.FROM) fromDate else toDate,
            onDismiss = { editingBound = null },
            onConfirm = { picked ->
                if (bound == DateBound.FROM) {
                    fromDate = picked
                    if (toDate != null && picked > toDate!!) toDate = null
                } else {
                    toDate = picked
                    if (fromDate != null && picked < fromDate!!) fromDate = null
                }
                editingBound = null
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(metric.title, style = MaterialTheme.typography.headlineSmall)
            Text(
                "Selecciona un rango para consultar los cierres registrados.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Desde", style = MaterialTheme.typography.labelMedium)
                    OutlinedButton(
                        onClick = { editingBound = DateBound.FROM },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(fromDate ?: "Elegir fecha", maxLines = 1)
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Hasta", style = MaterialTheme.typography.labelMedium)
                    OutlinedButton(
                        onClick = { editingBound = DateBound.TO },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(toDate ?: "Elegir fecha", maxLines = 1)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = {
                        fromDate = null
                        toDate = null
                    },
                    enabled = fromDate != null || toDate != null
                ) { Text("Limpiar filtros") }
                TextButton(onClick = onRefresh, enabled = !uiState.isLoading) {
                    androidx.compose.material3.Icon(
                        Icons.Outlined.Refresh,
                        contentDescription = null
                    )
                    Text("Actualizar")
                }
            }

            if (uiState.isLoading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            uiState.error?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text("TOTAL DEL PERIODO", style = MaterialTheme.typography.labelSmall)
                    Text(
                        metric.totalLabel(filtered),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "${filtered.size} de ${uiState.closings.size} días registrados",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (filtered.isEmpty() && !uiState.isLoading) {
                Text(
                    "No hay cierres registrados para las fechas seleccionadas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { closing ->
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    formatDate(closing.date),
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    metric.dailyLabel(closing),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun HomeMetric.dailyLabel(closing: DailyClosing): String = when (this) {
    HomeMetric.INCOME -> formatCurrency(closing.total)
    HomeMetric.CUSTOMERS -> "${closing.customers} clientes"
    HomeMetric.UNITS -> "${closing.units} unidades"
}

private fun HomeMetric.totalLabel(closings: List<DailyClosing>): String = when (this) {
    HomeMetric.INCOME -> formatCurrency(
        closings.fold(BigDecimal.ZERO) { acc, closing -> acc + closing.total }
    )
    HomeMetric.CUSTOMERS -> "${closings.sumOf { it.customers }} clientes"
    HomeMetric.UNITS -> "${closings.sumOf { it.units }} unidades"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeFilterDateDialog(
    initialDate: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val picker = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.let(::parseIsoDateUtc)
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = picker.selectedDateMillis != null,
                onClick = {
                    picker.selectedDateMillis?.let {
                        onConfirm(formatIsoDateUtc(it))
                    }
                }
            ) { Text("Seleccionar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    ) {
        DatePicker(state = picker, showModeToggle = false)
    }
}

private fun dateFormatterUtc(): SimpleDateFormat =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        isLenient = false
        timeZone = TimeZone.getTimeZone("UTC")
    }

private fun parseIsoDateUtc(value: String): Long? =
    dateFormatterUtc().parse(value)?.time

private fun formatIsoDateUtc(value: Long): String =
    dateFormatterUtc().format(Date(value))
