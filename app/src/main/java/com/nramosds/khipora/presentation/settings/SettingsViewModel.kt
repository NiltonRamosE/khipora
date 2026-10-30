package com.nramosds.khipora.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nramosds.khipora.domain.repository.BusinessRepository
import com.nramosds.khipora.utils.userErrorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class SettingsViewModel(private val repository: BusinessRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    fun load(businessId: Long) {
        if (businessId <= 0) return
        viewModelScope.launch {
            try {
                val business = repository.getById(businessId)
                _uiState.value = _uiState.value.copy(currentName = business?.name.orEmpty())
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(error = userErrorMessage(error))
            }
        }
    }

    fun save(businessId: Long, name: String) {
        if (_uiState.value.isLoading) return
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Ingresa el nombre de tu negocio.")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, savedId = null)
        viewModelScope.launch {
            try {
                val business = if (businessId > 0) {
                    repository.update(businessId, name.trim())
                } else {
                    repository.create(name.trim())
                }
                _uiState.value = SettingsUiState(currentName = business.name, savedId = business.id)
            } catch (error: CancellationException) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                throw error
            } catch (error: Exception) {
                _uiState.value =
                    _uiState.value.copy(isLoading = false, error = userErrorMessage(error))
            }
        }
    }

    fun linkBusiness(businessId: Long) {
        if (_uiState.value.isLoading) return
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, savedId = null)
        viewModelScope.launch {
            try {
                val business = repository.getById(businessId)
                _uiState.value = if (business == null) {
                    _uiState.value.copy(
                        isLoading = false,
                        error = "No encontramos ese negocio. Revisa el número e inténtalo de nuevo."
                    )
                } else {
                    SettingsUiState(currentName = business.name, savedId = business.id)
                }
            } catch (error: CancellationException) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                throw error
            } catch (error: Exception) {
                _uiState.value =
                    _uiState.value.copy(isLoading = false, error = userErrorMessage(error))
            }
        }
    }

    fun consumeSavedResult() {
        _uiState.value = _uiState.value.copy(savedId = null)
    }
}
