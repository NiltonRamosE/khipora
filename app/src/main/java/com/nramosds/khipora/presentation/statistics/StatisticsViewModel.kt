package com.nramosds.khipora.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nramosds.khipora.domain.repository.ClosingRepository
import com.nramosds.khipora.utils.userErrorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class StatisticsViewModel(private val repository: ClosingRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState = _uiState.asStateFlow()

    fun load(businessId: Long) {
        viewModelScope.launch {
            _uiState.value = StatisticsUiState()
            try {
                val closings = repository.getClosings(businessId)
                val paymentMethods = repository.getPaymentMethods()
                _uiState.value = StatisticsUiState(
                    isLoading = false,
                    closings = closings,
                    paymentMethods = paymentMethods,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.value =
                    StatisticsUiState(isLoading = false, error = userErrorMessage(error))
            }
        }
    }
}
