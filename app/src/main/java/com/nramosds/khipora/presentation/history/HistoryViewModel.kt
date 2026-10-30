package com.nramosds.khipora.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nramosds.khipora.domain.model.NewDailyClosing
import com.nramosds.khipora.domain.repository.ClosingRepository
import com.nramosds.khipora.utils.userErrorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

internal class HistoryViewModel(private val repository: ClosingRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null

    fun load(businessId: Long) {
        if (_uiState.value.isMutating) return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
            try {
                val closings = repository.getClosings(businessId)
                val methods = repository.getPaymentMethods()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    closings = closings,
                    paymentMethods = methods,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = userErrorMessage(error),
                )
            }
        }
    }

    fun update(businessId: Long, closingId: Long, values: NewDailyClosing) {
        if (_uiState.value.isMutating) return
        loadJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isMutating = true, isLoading = false, actionError = null, message = null
        )
        viewModelScope.launch {
            try {
                repository.update(closingId, values)
            } catch (error: CancellationException) {
                _uiState.value = _uiState.value.copy(isMutating = false)
                throw error
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isMutating = false,
                    actionError = "No se completó la edición: ${userErrorMessage(error)}. " +
                        "Comprueba el registro antes de reintentar; podrían haberse actualizado algunos pagos.",
                )
                return@launch
            }
            // La escritura ya terminó: el refresco es independiente para no inducir un doble guardado.
            finishMutation(businessId, closingId, HistoryAction.UPDATED)
        }
    }

    fun delete(businessId: Long, closingId: Long) {
        if (_uiState.value.isMutating) return
        loadJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isMutating = true, isLoading = false, actionError = null, message = null
        )
        viewModelScope.launch {
            try {
                repository.delete(closingId, businessId)
            } catch (error: CancellationException) {
                _uiState.value = _uiState.value.copy(isMutating = false)
                throw error
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isMutating = false,
                    actionError = "No se pudo eliminar: ${userErrorMessage(error)}",
                )
                return@launch
            }
            finishMutation(businessId, closingId, HistoryAction.DELETED)
        }
    }

    private suspend fun finishMutation(businessId: Long, closingId: Long, action: HistoryAction) {
        val success = if (action == HistoryAction.UPDATED) "Cierre actualizado." else "Cierre eliminado."
        try {
            val closings = repository.getClosings(businessId)
            _uiState.value = _uiState.value.copy(
                isMutating = false,
                closings = closings,
                actionError = null,
                mutationResult = HistoryMutationResult(closingId, action),
                message = success,
            )
        } catch (error: CancellationException) {
            _uiState.value = _uiState.value.copy(isMutating = false)
            throw error
        } catch (_: Exception) {
            _uiState.value = _uiState.value.copy(
                isMutating = false,
                actionError = null,
                mutationResult = HistoryMutationResult(closingId, action),
                message = "$success No se pudo refrescar el listado; pulsa Actualizar.",
            )
        }
    }

    fun clearActionError() {
        _uiState.value = _uiState.value.copy(actionError = null)
    }

    fun consumeMutationResult() {
        _uiState.value = _uiState.value.copy(mutationResult = null)
    }
}
