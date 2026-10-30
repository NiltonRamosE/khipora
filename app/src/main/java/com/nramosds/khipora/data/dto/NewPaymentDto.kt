package com.nramosds.khipora.data.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class NewPaymentDto(
    @SerializedName("cierre_id") val closingId: Long,
    @SerializedName("metodo_id") val paymentMethodId: Long,
    @SerializedName("monto") val amount: BigDecimal,
)
