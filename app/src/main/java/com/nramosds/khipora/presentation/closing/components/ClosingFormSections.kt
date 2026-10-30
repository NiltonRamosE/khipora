package com.nramosds.khipora.presentation.closing.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nramosds.khipora.R
import com.nramosds.khipora.domain.model.PaymentMethod
import com.nramosds.khipora.presentation.closing.ClosingFormState
import com.nramosds.khipora.presentation.closing.ClosingUiState
import com.nramosds.khipora.presentation.closing.parseAmount
import com.nramosds.khipora.utils.AppTheme
import com.nramosds.khipora.utils.components.AppCard
import com.nramosds.khipora.utils.components.AppTextField
import com.nramosds.khipora.utils.formatCurrency

@Composable
internal fun BusinessDaySection(
    formState: ClosingFormState,
    isEditable: Boolean,
    onDateClick: () -> Unit
) {
    AppCard {
        SectionHeader(R.drawable.calendario, "DATOS DE LA JORNADA")
        PickerField(
            "FECHA",
            formState.formattedDate,
            R.drawable.calendario,
            enabled = isEditable
        ) {
            onDateClick()
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppTextField(
                label = "UNIDADES",
                value = formState.units,
                onValueChange = { formState.units = it },
                modifier = Modifier.weight(1f),
                integerOnly = true,
                enabled = isEditable,
                placeholder = "0",
                isError = formState.hasUnitsError,
                supportingText = if (formState.hasUnitsError) {
                    "Número inválido"
                } else {
                    null
                }
            )

            AppTextField(
                label = "CLIENTES",
                value = formState.customers,
                onValueChange = { formState.customers = it },
                modifier = Modifier.weight(1f),
                integerOnly = true,
                enabled = isEditable,
                placeholder = "0",
                isError = formState.hasCustomersError,
                supportingText = if (formState.hasCustomersError) {
                    "Número inválido"
                } else {
                    null
                }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun BusinessDaySectionPreview() {
    AppTheme {
        val formState = remember { ClosingFormState() }
        Box(Modifier.padding(18.dp)) { BusinessDaySection(formState, true, {}) }
    }
}


@Composable
internal fun PaymentAmountsSection(
    formState: ClosingFormState,
    isEditable: Boolean,
    uiState: ClosingUiState,
    onRetry: () -> Unit
) {
    AppCard {
        SectionHeader(R.drawable.dinero, "INGRESOS DEL DÍA")
        if (uiState.isLoadingMethods) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text("Cargando métodos de pago…", style = MaterialTheme.typography.bodySmall)
        }
        uiState.paymentMethods.forEach { paymentMethod ->
            val value = formState.amounts[paymentMethod.id].orEmpty()
            val hasAmountError = formState.showValidation && parseAmount(value) == null
            AppTextField(
                label = "${paymentMethod.name} (S/)", value = value,
                onValueChange = {
                    formState.amounts = formState.amounts + (paymentMethod.id to it)
                },
                numeric = true, enabled = isEditable, placeholder = "0.00",
                leadingIconRes  = R.drawable.dinero, isError = hasAmountError,
                supportingText = if (hasAmountError) "Ingresa un monto; usa 0 si no hubo ingresos" else null,
            )
        }
        if (!uiState.isLoadingMethods && uiState.paymentMethods.isEmpty()) {
            Text(
                "No se pudieron obtener los métodos de pago.",
                color = MaterialTheme.colorScheme.error
            )
            OutlinedButton(onClick = { onRetry() }, enabled = isEditable) {
                Icon(Icons.Outlined.Refresh, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Volver a cargar")
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "TOTAL DEL DÍA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    formatCurrency(formState.total(uiState.paymentMethods)),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun PaymentAmountsSectionPreview() {
    AppTheme {
        val formState = remember { ClosingFormState() }
        Box(Modifier.padding(18.dp)) {
            PaymentAmountsSection(
                formState,
                true,
                ClosingUiState(
                    paymentMethods = listOf(
                        PaymentMethod(1, "Efectivo"),
                        PaymentMethod(2, "Yape")
                    ), isLoadingMethods = false
                ),
                {})
        }
    }
}


@Composable
internal fun CustomerDetailsSection(
    formState: ClosingFormState,
    isEditable: Boolean
) {
    AppCard {
        SectionHeader(R.drawable.reloj, "HORA PICO")

        ClosingScheduleSelector(
            formState = formState,
            enabled = isEditable
        )

        ClosingChoiceSelector(
            title = "PÚBLICO PREDOMINANTE",
            options = listOf(
                ClosingChoice("ninos", "Niños"),
                ClosingChoice("jovenes", "Jóvenes"),
                ClosingChoice("adultos", "Adultos")
            ),
            selectedValue = formState.audience,
            enabled = isEditable,
            onSelected = { formState.audience = it },
            iconRes = R.drawable.clientes_potenciales
        )

        ClosingChoiceSelector(
            title = "MOTIVO DE CONSUMO",
            options = listOf(
                ClosingChoice("salud", "Salud"),
                ClosingChoice("calor", "Calor"),
                ClosingChoice("costumbre", "Costumbre")
            ),
            selectedValue = formState.consumptionReason,
            enabled = isEditable,
            onSelected = {
                formState.consumptionReason = it
            },
            iconRes = R.drawable.consumo
        )

        ClosingChoiceSelector(
            title = "ATRIBUTO DESTACADO",
            options = listOf(
                ClosingChoice("precio_justo", "Precio justo"),
                ClosingChoice("limpieza", "Limpieza"),
                ClosingChoice("atencion", "Atención")
            ),
            selectedValue = formState.attribute,
            enabled = isEditable,
            onSelected = {
                formState.attribute = it
            },
            iconRes = R.drawable.atributos
        )

        AppTextField(
            label = "Notas · opcional",
            value = formState.notes,
            onValueChange = { formState.notes = it },
            singleLine = false,
            enabled = isEditable,
            placeholder = "¿Algo que quieras recordar de hoy?"
        )
    }
}

private data class ScheduleOption(
    val label: String,
    val description: String,
    val start: String,
    val end: String
)

private val schedules = listOf(
    ScheduleOption(
        "9:00 a. m. – 12:00 p. m.",
        "Mañana",
        "09:00",
        "12:00"
    ),
    ScheduleOption(
        "12:00 p. m. – 3:00 p. m.",
        "Mediodía",
        "12:00",
        "15:00"
    ),
    ScheduleOption(
        "3:00 p. m. – 5:00 p. m.",
        "Tarde",
        "15:00",
        "17:00"
    )
)

@Composable
private fun ClosingScheduleSelector(
    formState: ClosingFormState,
    enabled: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        schedules.forEach { schedule ->
            val selected =
                formState.startTime == schedule.start &&
                    formState.endTime == schedule.end

            OutlinedCard(
                onClick = {
                    formState.selectSchedule(
                        schedule.start,
                        schedule.end
                    )
                },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    1.dp,
                    if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }
                ),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    RadioButton(
                        selected = selected,
                        onClick = null,
                        enabled = enabled
                    )

                    Column(
                        verticalArrangement =
                            Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            schedule.label,
                            style =
                                MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            schedule.description,
                            style =
                                MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme
                                .onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (formState.startTime.isNotBlank()) {
            TextButton(
                onClick = {
                    formState.selectSchedule("", "")
                },
                enabled = enabled
            ) {
                Text("Quitar horario")
            }
        }
    }
}
@Preview(showBackground = true, widthDp = 400)
@Composable
private fun CustomerDetailsSectionPreview() {
    AppTheme {
        val formState = remember { ClosingFormState() }
        Box(Modifier.padding(18.dp)) { CustomerDetailsSection(formState, true) }
    }
}


@Composable
internal fun SectionHeader(
    @DrawableRes iconRes: Int,
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            contentScale = ContentScale.Fit
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
