package com.nramosds.khipora.data.repository

import com.nramosds.khipora.core.network.SupabaseApi
import com.nramosds.khipora.data.dto.NewDailyClosingDto
import com.nramosds.khipora.data.dto.NewPaymentDto
import com.nramosds.khipora.domain.exception.ClosingSaveException
import com.nramosds.khipora.domain.model.DailyClosing
import com.nramosds.khipora.domain.model.NewDailyClosing
import com.nramosds.khipora.domain.model.Payment
import com.nramosds.khipora.domain.model.PaymentMethod
import com.nramosds.khipora.domain.repository.ClosingRepository
import java.io.IOException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class ClosingRepositoryImpl(private val api: SupabaseApi) : ClosingRepository {

    override suspend fun getClosings(businessId: Long): List<DailyClosing> {
        return api.closings(businessId = "eq.$businessId").map { closing ->
            DailyClosing(
                id = closing.id,
                date = closing.date,
                units = closing.units,
                customers = closing.customers,
                startTime = closing.startTime,
                endTime = closing.endTime,
                audience = closing.audience,
                notes = closing.notes,
                payments = closing.payments.map { payment ->
                    Payment(payment.paymentMethodId, payment.amount)
                },
                consumptionReason = closing.consumptionReason,
                attribute = closing.attribute,
            )
        }
    }

    override suspend fun getPaymentMethods(): List<PaymentMethod> {
        return api.getPaymentMethods().map { method ->
            PaymentMethod(id = method.id, name = method.name)
        }
    }

    override suspend fun save(closing: NewDailyClosing): Long {
        val request = NewDailyClosingDto(
            businessId = closing.businessId,
            date = closing.date,
            units = closing.units,
            customers = closing.customers,
            startTime = closing.startTime,
            endTime = closing.endTime,
            audience = closing.audience,
            notes = closing.notes,
            consumptionReason = closing.consumptionReason,
            attribute = closing.attribute,
        )
        val closingId = api.createClosing(request).single().id

        if (closing.payments.isEmpty()) return closingId

        val payments = closing.payments.map { payment ->
            NewPaymentDto(
                closingId = closingId,
                paymentMethodId = payment.paymentMethodId,
                amount = payment.amount,
            )
        }

        try {
            api.createPayments(payments)
        } catch (error: HttpException) {
            val wasDeleted = rollbackClosing(closingId)
            throw ClosingSaveException(requiresReview = !wasDeleted, cause = error)
        } catch (error: IOException) {
            throw ClosingSaveException(requiresReview = true, cause = error)
        }

        return closingId
    }

    private suspend fun rollbackClosing(closingId: Long): Boolean {
        return withContext(NonCancellable) {
            try {
                api.deleteClosing(id = "eq.$closingId").singleOrNull()?.id == closingId
            } catch (_: Exception) {
                false
            }
        }
    }
}
