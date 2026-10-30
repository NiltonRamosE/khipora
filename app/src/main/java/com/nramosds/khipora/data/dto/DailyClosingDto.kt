package com.nramosds.khipora.data.dto

import com.google.gson.annotations.SerializedName

data class DailyClosingDto(
    val id: Long,
    @SerializedName("negocio_id") val businessId: Long,
    @SerializedName("fecha") val date: String,
    @SerializedName("cantidad") val units: Int,
    @SerializedName("clientes") val customers: Int,
    @SerializedName("hora_inicio") val startTime: String?,
    @SerializedName("hora_fin") val endTime: String?,
    @SerializedName("publico") val audience: String,
    @SerializedName("notas") val notes: String?,
    @SerializedName("pagos") val payments: List<PaymentDto> = emptyList(),
    @SerializedName("motivo_consumo") val consumptionReason: String? = null,
    @SerializedName("atributo") val attribute: String? = null,
)
