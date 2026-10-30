package com.nramosds.khipora.presentation.closing

import com.nramosds.khipora.domain.model.PaymentMethod

data class ClosingUiState(
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val isLoadingMethods: Boolean = true,
    val isSaving: Boolean = false,
    val savedId: Long? = null,
    val error: String? = null,
    val saveError: String? = null,
)

