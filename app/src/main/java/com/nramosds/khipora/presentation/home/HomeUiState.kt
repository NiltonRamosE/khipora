package com.nramosds.khipora.presentation.home

import com.nramosds.khipora.domain.model.DailyClosing
import java.math.BigDecimal

data class HomeUiState(
    val isLoading: Boolean = true,
    val closings: List<DailyClosing> = emptyList(),
    val error: String? = null,
) {
    val income: BigDecimal get() = closings.fold(BigDecimal.ZERO) { acc, c -> acc + c.total }
    val customers: Int get() = closings.sumOf { it.customers }
    val units: Int get() = closings.sumOf { it.units }
}

