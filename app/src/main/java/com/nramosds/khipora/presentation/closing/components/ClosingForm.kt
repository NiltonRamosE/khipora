package com.nramosds.khipora.presentation.closing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nramosds.khipora.domain.model.PaymentMethod
import com.nramosds.khipora.presentation.closing.ClosingFormState
import com.nramosds.khipora.presentation.closing.ClosingUiState
import com.nramosds.khipora.utils.AppTheme

@Composable
internal fun ClosingForm(
    formState: ClosingFormState,
    uiState: ClosingUiState,
    onRetry: () -> Unit,
    onSave: () -> Unit
) {
    val focus = LocalFocusManager.current
    val isEditable = !uiState.isSaving && uiState.savedId == null && uiState.saveError == null
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var selectedTimeField by rememberSaveable { mutableStateOf<String?>(null) }
    if (showDatePicker) {
        ClosingDatePickerDialog(formState.date, onDismiss = { showDatePicker = false }) {
            formState.date = it
            showDatePicker = false
        }
    }
    Column(Modifier.fillMaxSize().imePadding()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Outlined.AssignmentTurnedIn,
                        null,
                        Modifier.padding(14.dp).size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text("Cierre del día", style = MaterialTheme.typography.headlineSmall)
                }
            }
            BusinessDaySection(formState, isEditable) {
                focus.clearFocus()
                showDatePicker = true
            }
            PaymentAmountsSection(formState, isEditable, uiState, onRetry)
            CustomerDetailsSection(
                formState = formState,
                isEditable = isEditable
            )
            uiState.error?.let { message ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ErrorOutline,
                            null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        SaveClosingBar(
            isSaving = uiState.isSaving,
            enabled = isEditable && !uiState.isLoadingMethods && uiState.paymentMethods.isNotEmpty(),
            onSave = {
                focus.clearFocus()
                onSave()
            },
        )
    }
}

@Composable
private fun SaveClosingBar(isSaving: Boolean, enabled: Boolean, onSave: () -> Unit) {
    Surface(shadowElevation = 4.dp, color = MaterialTheme.colorScheme.surface) {
        Button(
            onClick = onSave, enabled = enabled,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp)
                .heightIn(min = 54.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            if (isSaving) CircularProgressIndicator(
                Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            else Icon(Icons.Outlined.CheckCircleOutline, null, Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(if (isSaving) "Guardando cierre…" else "Guardar cierre")
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
private fun ClosingFormPreview() {
    AppTheme {
        val formState = remember {
            ClosingFormState().apply {
                units = "24"
                customers = "15"
                amounts = mapOf(1L to "120.00", 2L to "80.00")
            }
        }
        ClosingForm(
            formState,
            ClosingUiState(
                paymentMethods = listOf(
                    PaymentMethod(1, "Efectivo"),
                    PaymentMethod(2, "Yape")
                ), isLoadingMethods = false
            ),
            {},
            {})
    }
}
