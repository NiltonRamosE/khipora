package com.nramosds.khipora.presentation.statistics

import com.nramosds.khipora.domain.model.DailyClosing
import com.nramosds.khipora.domain.model.PaymentMethod

internal data class StatisticsUiState(
    val isLoading: Boolean = true,
    val closings: List<DailyClosing> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val error: String? = null,
)

