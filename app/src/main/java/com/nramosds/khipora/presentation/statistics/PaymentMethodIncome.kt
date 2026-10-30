package com.nramosds.khipora.presentation.statistics

import java.math.BigDecimal

internal data class PaymentMethodIncome(
    val name: String,
    val amount: BigDecimal,
    val fraction: Float
)
