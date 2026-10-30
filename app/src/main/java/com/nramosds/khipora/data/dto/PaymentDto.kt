package com.nramosds.khipora.data.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class PaymentDto(
    @SerializedName("metodo_id") val paymentMethodId: Long,
    @SerializedName("monto") val amount: BigDecimal,
)
