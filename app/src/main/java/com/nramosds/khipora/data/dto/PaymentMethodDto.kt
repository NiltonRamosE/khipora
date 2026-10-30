package com.nramosds.khipora.data.dto

import com.google.gson.annotations.SerializedName

data class PaymentMethodDto(
    val id: Long,
    @SerializedName("nombre") val name: String,
    @SerializedName("activo") val active: Boolean
)
