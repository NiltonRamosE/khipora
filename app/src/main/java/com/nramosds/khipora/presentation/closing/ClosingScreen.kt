package com.nramosds.khipora.presentation.closing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nramosds.khipora.AppContainer
import com.nramosds.khipora.presentation.closing.components.ClosingForm
import com.nramosds.khipora.presentation.closing.components.ClosingResultDialog
import com.nramosds.khipora.utils.formatDate
import com.nramosds.khipora.utils.viewModelFactory

@Composable
fun ClosingScreen(businessId: Long, container: AppContainer, onSaved: () -> Unit) {
    val viewModel: ClosingViewModel =
        viewModel(factory = viewModelFactory { ClosingViewModel(container.closingRepository) })
    val uiState by viewModel.uiState.collectAsState()
    val formState =
        rememberSaveable(businessId, saver = ClosingFormState.Saver) { ClosingFormState() }
    LaunchedEffect(businessId) { viewModel.loadPaymentMethods() }

    ClosingForm(
        formState, uiState, onRetry = { viewModel.loadPaymentMethods() },
        onSave = {
            val closing = formState.createClosing(businessId, uiState.paymentMethods)
            if (closing == null) viewModel.showError("Revisa los campos marcados antes de guardar.")
            else viewModel.save(closing)
        }
    )
    if (uiState.savedId != null) {
        ClosingResultDialog(
            isSuccess = true,
            message = "El cierre del ${formatDate(formState.date)} y sus pagos se guardaron correctamente.",
            onConfirm = {
                viewModel.clearSavedResult()
                onSaved()
            },
        )
    }
    uiState.saveError?.let { message ->
        ClosingResultDialog(false, message, onConfirm = viewModel::clearSaveError)
    }
}
