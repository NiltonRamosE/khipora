package com.nramosds.khipora.domain.model

import java.math.BigDecimal

data class Payment(
    val paymentMethodId: Long,
    val amount: BigDecimal
)
