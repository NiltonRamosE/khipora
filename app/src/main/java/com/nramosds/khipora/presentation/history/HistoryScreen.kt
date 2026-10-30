package com.nramosds.khipora.presentation.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nramosds.khipora.AppContainer
import com.nramosds.khipora.domain.model.DailyClosing
import com.nramosds.khipora.domain.model.Payment
import com.nramosds.khipora.domain.model.NewDailyClosing
import com.nramosds.khipora.utils.AppTheme
import com.nramosds.khipora.utils.components.AppCard
import com.nramosds.khipora.utils.components.ClosingSummaryCard
import com.nramosds.khipora.utils.formatDate
import com.nramosds.khipora.utils.viewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun HistoryScreen(businessId: Long, container: AppContainer) {
    val viewModel: HistoryViewModel =
        viewModel(factory = viewModelFactory { HistoryViewModel(container.closingRepository) })
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(businessId) { viewModel.load(businessId) }
    HistoryContent(
        businessId = businessId,
        uiState = uiState,
        onRefresh = { viewModel.load(businessId) },
        onUpdate = { id, values -> viewModel.update(businessId, id, values) },
        onDelete = { id -> viewModel.delete(businessId, id) },
        onMutationConsumed = viewModel::consumeMutationResult,
        onClearActionError = viewModel::clearActionError,
    )
}

private enum class DateField { FROM, TO }

@Composable
internal fun HistoryContent(
    businessId: Long,
    uiState: HistoryUiState,
    onRefresh: () -> Unit,
    onUpdate: (Long, NewDailyClosing) -> Unit,
    onDelete: (Long) -> Unit,
    onMutationConsumed: () -> Unit,
    onClearActionError: () -> Unit,
) {
    var fromDate by rememberSaveable { mutableStateOf<String?>(null) }
    var toDate by rememberSaveable { mutableStateOf<String?>(null) }
    var editingDate by remember { mutableStateOf<DateField?>(null) }
    var selectedClosingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editingClosingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deletingClosingId by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(uiState.mutationResult) {
        uiState.mutationResult?.let { result ->
            when (result.action) {
                HistoryAction.UPDATED -> editingClosingId = null
                HistoryAction.DELETED -> {
                    deletingClosingId = null
                    if (selectedClosingId == result.closingId) selectedClosingId = null
                }
            }
            onMutationConsumed()
        }
    }

    val filtered = remember(uiState.closings, fromDate, toDate) {
        uiState.closings.filter { closing ->
            (fromDate == null || closing.date >= fromDate!!) &&
                (toDate == null || closing.date <= toDate!!)
        }.sortedWith(compareByDescending<DailyClosing> { it.date }.thenByDescending { it.id })
    }

    editingDate?.let { field ->
        HistoryDatePickerDialog(
            date = if (field == DateField.FROM) fromDate else toDate,
            onDismiss = { editingDate = null },
            onSelected = { selected ->
                when (field) {
                    DateField.FROM -> {
                        fromDate = selected
                        if (toDate != null && selected > toDate!!) toDate = null
                    }
                    DateField.TO -> {
                        toDate = selected
                        if (fromDate != null && selected < fromDate!!) fromDate = null
                    }
                }
                editingDate = null
            },
        )
    }

    uiState.closings.firstOrNull { it.id == selectedClosingId }?.let { closing ->
        HistoryDetailSheet(
            closing = closing,
            paymentMethods = uiState.paymentMethods,
            onDismiss = { selectedClosingId = null },
        )
    }

    uiState.closings.firstOrNull { it.id == editingClosingId }?.let { closing ->
        HistoryEditSheet(
            closing = closing,
            businessId = businessId,
            paymentMethods = uiState.paymentMethods,
            isSaving = uiState.isMutating,
            errorMessage = uiState.actionError,
            onDismiss = {
                if (!uiState.isMutating) {
                    editingClosingId = null
                    onClearActionError()
                }
            },
            onSave = { values -> onUpdate(closing.id, values) },
        )
    }

    uiState.closings.firstOrNull { it.id == deletingClosingId }?.let { closing ->
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isMutating) {
                    deletingClosingId = null
                    onClearActionError()
                }
            },
            title = { Text("¿Eliminar este cierre?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Se eliminará el cierre del ${formatDate(closing.date)}. " +
                        "Esta acción no se puede deshacer.")
                    uiState.actionError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { onDelete(closing.id) },
                    enabled = !uiState.isMutating,
                ) { Text(if (uiState.isMutating) "Eliminando…" else "Eliminar") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        deletingClosingId = null
                        onClearActionError()
                    },
                    enabled = !uiState.isMutating,
                ) { Text("Cancelar") }
            },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Historial de cierres", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Consulta tus jornadas y abre un registro para ver todos sus datos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            AppCard {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary)
                    Text("Filtrar por fecha", style = MaterialTheme.typography.titleMedium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Column(modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Desde", style = MaterialTheme.typography.labelSmall)
                        OutlinedButton(
                            onClick = { editingDate = DateField.FROM },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(fromDate ?: "Elegir", maxLines = 1) }
                    }
                    Column(modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Hasta", style = MaterialTheme.typography.labelSmall)
                        OutlinedButton(
                            onClick = { editingDate = DateField.TO },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(toDate ?: "Elegir", maxLines = 1) }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(
                        onClick = { fromDate = null; toDate = null },
                        enabled = fromDate != null || toDate != null,
                    ) { Text("Limpiar") }
                    TextButton(onClick = onRefresh, enabled = !uiState.isLoading) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null)
                        Text("Actualizar")
                    }
                }
            }
        }

        uiState.message?.let { message ->
            item {
                AppCard {
                    Text(message, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (uiState.isLoading) {
            item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        }

        uiState.error?.let { message ->
            item {
                AppCard {
                    Text(message, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = onRefresh) { Text("Reintentar") }
                }
            }
        }

        if (!uiState.isLoading || uiState.closings.isNotEmpty()) {
            item {
                Text(
                    "${filtered.size} registros de ${uiState.closings.size} en total",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (filtered.isEmpty() && uiState.error == null) {
                item {
                    AppCard {
                        Icon(Icons.Outlined.History, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                        Text("No hay cierres en el rango seleccionado.")
                    }
                }
            }
            items(filtered, key = { it.id }) { closing ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !uiState.isMutating) {
                                selectedClosingId = closing.id
                            }
                    ) {
                        ClosingSummaryCard(closing = closing, showDetails = true)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        OutlinedButton(
                            enabled = !uiState.isMutating,
                            onClick = {
                                onClearActionError()
                                editingClosingId = closing.id
                            },
                        ) {
                            Icon(Icons.Outlined.Edit, contentDescription = null)
                            Text(" Editar")
                        }
                        TextButton(
                            enabled = !uiState.isMutating,
                            onClick = {
                                onClearActionError()
                                deletingClosingId = closing.id
                            },
                        ) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = null,
                                tint = MaterialTheme.colorScheme.error)
                            Text(" Eliminar", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HistoryDatePickerDialog(
    date: String?,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit,
) {
    val picker = rememberDatePickerState(
        initialSelectedDateMillis = date?.let { isoFormatter().parse(it)?.time },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = picker.selectedDateMillis != null,
                onClick = {
                    picker.selectedDateMillis?.let { onSelected(isoFormatter().format(Date(it))) }
                },
            ) { Text("Seleccionar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    ) { DatePicker(state = picker, showModeToggle = false) }
}

private fun isoFormatter(): SimpleDateFormat =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        isLenient = false
        timeZone = TimeZone.getTimeZone("UTC")
    }

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun HistoryContentPreview() {
    AppTheme {
        HistoryContent(
            businessId = 1,
            uiState = HistoryUiState(
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
                        notes = "Buen día",
                        payments = listOf(Payment(1, "120.00".toBigDecimal())),
                    ),
                ),
            ),
            onRefresh = {},
            onUpdate = { _, _ -> },
            onDelete = {},
            onMutationConsumed = {},
            onClearActionError = {},
        )
    }
}
