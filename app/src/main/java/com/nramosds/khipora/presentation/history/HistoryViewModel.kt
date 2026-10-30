package com.nramosds.khipora.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nramosds.khipora.domain.repository.ClosingRepository
import com.nramosds.khipora.utils.userErrorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class HistoryViewModel(private val repository: ClosingRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState = _uiState.asStateFlow()

    fun load(businessId: Long) {
        if (_uiState.value.isLoading && _uiState.value.closings.isNotEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val closings = repository.getClosings(businessId)
                val methods = repository.getPaymentMethods()
                _uiState.value = HistoryUiState(
                    isLoading = false,
                    closings = closings,
                    paymentMethods = methods,
                )
            } catch (error: CancellationException) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                throw error
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = userErrorMessage(error),
                )
            }
        }
    }
}
