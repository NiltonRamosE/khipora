package com.nramosds.khipora.domain.model


data class NewDailyClosing(
    val businessId: Long,
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
)
