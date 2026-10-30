package com.nramosds.khipora.presentation.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.nramosds.khipora.domain.model.DailyClosing
import com.nramosds.khipora.domain.model.NewDailyClosing
import com.nramosds.khipora.domain.model.Payment
import com.nramosds.khipora.domain.model.PaymentMethod
import com.nramosds.khipora.utils.components.AppTextField
import com.nramosds.khipora.utils.formatDate

private data class HistorySchedule(val label: String, val start: String, val end: String)

private val availableSchedules = listOf(
    HistorySchedule("9:00 a. m. – 12:00 p. m.", "09:00", "12:00"),
    HistorySchedule("12:00 p. m. – 3:00 p. m.", "12:00", "15:00"),
    HistorySchedule("3:00 p. m. – 5:00 p. m.", "15:00", "17:00"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HistoryEditSheet(
    closing: DailyClosing,
    businessId: Long,
    paymentMethods: List<PaymentMethod>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (NewDailyClosing) -> Unit,
) {
    var date by remember(closing.id) { mutableStateOf(closing.date) }
    var units by remember(closing.id) { mutableStateOf(closing.units.toString()) }
    var customers by remember(closing.id) { mutableStateOf(closing.customers.toString()) }
    var startTime by remember(closing.id) { mutableStateOf(closing.startTime.orEmpty()) }
    var endTime by remember(closing.id) { mutableStateOf(closing.endTime.orEmpty()) }
    var audience by remember(closing.id) { mutableStateOf(closing.audience) }
    var consumptionReason by remember(closing.id) { mutableStateOf(closing.consumptionReason.orEmpty()) }
    var attribute by remember(closing.id) { mutableStateOf(closing.attribute.orEmpty()) }
    var notes by remember(closing.id) { mutableStateOf(closing.notes.orEmpty()) }
    var amounts by remember(closing.id, paymentMethods) {
        mutableStateOf(paymentMethods.associate { method ->
            method.id to (closing.payments.firstOrNull { it.paymentMethodId == method.id }
                ?.amount?.toPlainString() ?: "0.00")
        })
    }
    var showDatePicker by remember(closing.id) { mutableStateOf(false) }
    var validationError by remember(closing.id) { mutableStateOf<String?>(null) }

    if (showDatePicker) {
        HistoryDatePickerDialog(
            date = date,
            onDismiss = { showDatePicker = false },
            onSelected = {
                date = it
                showDatePicker = false
            },
        )
    }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = screenHeight * 0.88f)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Editar cierre", style = MaterialTheme.typography.headlineSmall)
            Text("Registro #${closing.id}", color = MaterialTheme.colorScheme.onSurfaceVariant)

            OutlinedButton(
                onClick = { showDatePicker = true },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Fecha: ${formatDate(date)}") }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AppTextField(
                    label = "Unidades", value = units,
                    onValueChange = { units = it },
                    modifier = Modifier.weight(1f),
                    integerOnly = true, placeholder = "0", enabled = !isSaving,
                )
                AppTextField(
                    label = "Clientes", value = customers,
                    onValueChange = { customers = it },
                    modifier = Modifier.weight(1f),
                    integerOnly = true, placeholder = "0", enabled = !isSaving,
                )
            }

            HorizontalDivider()
            Text("Horario de atención", style = MaterialTheme.typography.titleMedium)
            HistoryChoiceRow(
                options = listOf("Sin horario" to "") + availableSchedules.map { it.label to it.start },
                selectedValue = startTime,
                enabled = !isSaving,
                allowEmpty = false,
                onSelected = { selected ->
                    val schedule = availableSchedules.firstOrNull { it.start == selected }
                    startTime = schedule?.start.orEmpty()
                    endTime = schedule?.end.orEmpty()
                },
            )
            if (startTime.isNotBlank() && availableSchedules.none {
                    it.start == startTime && it.end == endTime
                }) {
                Text(
                    "El horario anterior ($startTime - $endTime) no corresponde a los turnos actuales. " +
                        "Selecciona uno de los horarios disponibles.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            HorizontalDivider()
            Text("Público predominante", style = MaterialTheme.typography.titleMedium)
            HistoryChoiceRow(
                listOf("Niños" to "ninos", "Jóvenes" to "jovenes", "Adultos" to "adultos"),
                selectedValue = audience,
                enabled = !isSaving,
                allowEmpty = false,
                onSelected = { audience = it },
            )

            Text("Motivo de consumo", style = MaterialTheme.typography.titleMedium)
            HistoryChoiceRow(
                listOf("Salud" to "salud", "Calor" to "calor", "Costumbre" to "costumbre"),
                selectedValue = consumptionReason,
                enabled = !isSaving,
                onSelected = { consumptionReason = it },
            )

            Text("Atributo destacado", style = MaterialTheme.typography.titleMedium)
            HistoryChoiceRow(
                listOf(
                    "Precio justo" to "precio_justo",
                    "Limpieza" to "limpieza",
                    "Atención" to "atencion",
                ),
                selectedValue = attribute,
                enabled = !isSaving,
                onSelected = { attribute = it },
            )

            HorizontalDivider()
            Text("Métodos de pago", style = MaterialTheme.typography.titleMedium)
            paymentMethods.forEach { method ->
                AppTextField(
                    label = "${method.name} (S/)",
                    value = amounts[method.id].orEmpty(),
                    onValueChange = { amounts = amounts + (method.id to it) },
                    numeric = true,
                    placeholder = "0.00",
                    enabled = !isSaving,
                )
            }
            if (paymentMethods.isEmpty()) {
                Text(
                    "No hay métodos de pago disponibles; no se puede editar este cierre.",
                    color = MaterialTheme.colorScheme.error,
                )
            }

            AppTextField(
                label = "Notas (opcional)", value = notes,
                onValueChange = { notes = it },
                singleLine = false, enabled = !isSaving,
            )

            (validationError ?: errorMessage)?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val unitCount = units.toIntOrNull()
                    val customerCount = customers.toIntOrNull()
                    val validHours = (startTime.isBlank() && endTime.isBlank()) ||
                        availableSchedules.any { it.start == startTime && it.end == endTime }
                    val allowedAudience = audience in setOf("ninos", "jovenes", "adultos")
                    val parsedAmounts = paymentMethods.map { method ->
                        val raw = amounts[method.id].orEmpty().replace(',', '.')
                        method.id to raw.toBigDecimalOrNull()
                    }
                    val validAmounts = parsedAmounts.all { (_, amount) ->
                        amount != null && amount.signum() >= 0 && amount.scale() <= 2
                    }
                    when {
                        date.isBlank() || unitCount == null || unitCount < 0 ||
                            customerCount == null || customerCount < 0 -> {
                            validationError = "Ingresa unidades y clientes válidos."
                        }
                        !validHours -> validationError = "Selecciona un horario válido."
                        !allowedAudience -> validationError = "Selecciona el público predominante."
                        paymentMethods.isEmpty() || !validAmounts -> {
                            validationError = "Revisa los montos; usa 0.00 cuando no hubo ingresos."
                        }
                        else -> {
                            validationError = null
                            // Conserva pagos de métodos antiguos que ya no están en el catálogo activo.
                            val visibleIds = paymentMethods.map { it.id }.toSet()
                            val hiddenPayments = closing.payments.filter {
                                it.paymentMethodId !in visibleIds
                            }
                            val payments = parsedAmounts.map { (id, amount) ->
                                Payment(id, requireNotNull(amount))
                            } + hiddenPayments
                            onSave(
                                NewDailyClosing(
                                    businessId = businessId,
                                    date = date,
                                    units = unitCount,
                                    customers = customerCount,
                                    startTime = startTime.ifBlank { null },
                                    endTime = endTime.ifBlank { null },
                                    audience = audience,
                                    notes = notes.ifBlank { null },
                                    payments = payments,
                                    consumptionReason = consumptionReason.ifBlank { null },
                                    attribute = attribute.ifBlank { null },
                                )
                            )
                        }
                    }
                },
                enabled = !isSaving && paymentMethods.isNotEmpty(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isSaving) CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                    strokeWidth = 2.dp,
                )
                Text(if (isSaving) "Guardando…" else "Guardar cambios")
            }
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Cancelar") }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HistoryChoiceRow(
    options: List<Pair<String, String>>,
    selectedValue: String,
    enabled: Boolean,
    allowEmpty: Boolean = true,
    onSelected: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (label, value) ->
            FilterChip(
                selected = selectedValue == value,
                enabled = enabled,
                onClick = {
                    onSelected(if (allowEmpty && selectedValue == value) "" else value)
                },
                label = { Text(label) },
            )
        }
    }
}
