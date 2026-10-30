package com.nramosds.khipora.presentation.history

import com.nramosds.khipora.domain.model.DailyClosing
import com.nramosds.khipora.domain.model.PaymentMethod

internal data class HistoryUiState(
    val isLoading: Boolean = true,
    val closings: List<DailyClosing> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val error: String? = null,
)
