package com.nramosds.khipora.presentation.history

import com.nramosds.khipora.domain.model.DailyClosing
import com.nramosds.khipora.domain.model.PaymentMethod

internal enum class HistoryAction { UPDATED, DELETED }

internal data class HistoryMutationResult(val closingId: Long, val action: HistoryAction)

internal data class HistoryUiState(
    val isLoading: Boolean = true,
    val closings: List<DailyClosing> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val error: String? = null,
    val isMutating: Boolean = false,
    val actionError: String? = null,
    val message: String? = null,
    val mutationResult: HistoryMutationResult? = null,
)
