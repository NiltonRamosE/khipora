package com.nramosds.khipora.domain.model

import java.math.BigDecimal

data class DailyClosing(
    val id: Long,
    val date: String,
    val units: Int,
    val customers: Int,
    val startTime: String?,
    val endTime: String?,
    val audience: String,
    val notes: String?,
    val payments: List<Payment>,
    val consumptionReason: String? = null,
    val attribute: String? = null,
) {
    val total: BigDecimal get() = payments.fold(BigDecimal.ZERO) { acc, payment -> acc + payment.amount }
}
