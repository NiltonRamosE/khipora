package com.nramosds.khipora.domain.repository

import com.nramosds.khipora.domain.model.DailyClosing
import com.nramosds.khipora.domain.model.NewDailyClosing
import com.nramosds.khipora.domain.model.PaymentMethod

interface ClosingRepository {
    suspend fun getClosings(businessId: Long): List<DailyClosing>
    suspend fun getPaymentMethods(): List<PaymentMethod>
    suspend fun save(closing: NewDailyClosing): Long
    suspend fun update(closingId: Long, closing: NewDailyClosing)
    suspend fun delete(closingId: Long, businessId: Long)
}
