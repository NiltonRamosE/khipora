package com.nramosds.khipora.presentation.closing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nramosds.khipora.domain.model.NewDailyClosing
import com.nramosds.khipora.domain.repository.ClosingRepository
import com.nramosds.khipora.utils.userErrorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClosingViewModel(private val repository: ClosingRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ClosingUiState())
    val uiState = _uiState.asStateFlow()

    fun loadPaymentMethods() {
        if (_uiState.value.isLoadingMethods && _uiState.value.paymentMethods.isNotEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMethods = true, error = null)
            try {
                val methods = repository.getPaymentMethods()
                _uiState.value =
                    _uiState.value.copy(paymentMethods = methods, isLoadingMethods = false)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.value =
                    _uiState.value.copy(isLoadingMethods = false, error = userErrorMessage(error))
            }
        }
    }

    fun save(closing: NewDailyClosing) {
        if (_uiState.value.isSaving || _uiState.value.savedId != null) return
        _uiState.value = _uiState.value.copy(isSaving = true, error = null, saveError = null)

        viewModelScope.launch {
            try {
                val closingId = repository.save(closing)
                _uiState.value = _uiState.value.copy(isSaving = false, savedId = closingId)
            } catch (error: CancellationException) {
                _uiState.value = _uiState.value.copy(isSaving = false)
                throw error
            } catch (error: Exception) {
                _uiState.value =
                    _uiState.value.copy(isSaving = false, saveError = userErrorMessage(error))
            }
        }
    }

    fun clearSavedResult() {
        _uiState.value = _uiState.value.copy(savedId = null)
    }

    fun clearSaveError() {
        _uiState.value = _uiState.value.copy(saveError = null)
    }

    fun showError(message: String) {
        _uiState.value = _uiState.value.copy(error = message)
    }
}
