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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
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
                consumptionReason = closing.consumptionReason,
                attribute = closing.attribute,
                payments = closing.payments.map { payment ->
                    Payment(payment.paymentMethodId, payment.amount)
                },
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

    override suspend fun update(closingId: Long, closing: NewDailyClosing) {
        val previous = getClosings(closing.businessId).firstOrNull { it.id == closingId }
            ?: throw IllegalStateException("El cierre ya no existe en este negocio.")

        // JSONObject.NULL fuerza el envío de null, incluso si tu serializador omite nulos.
        val closingPayload = JSONObject().apply {
            put("fecha", closing.date)
            put("cantidad", closing.units)
            put("clientes", closing.customers)
            put("hora_inicio", closing.startTime ?: JSONObject.NULL)
            put("hora_fin", closing.endTime ?: JSONObject.NULL)
            put("publico", closing.audience)
            put("notas", closing.notes ?: JSONObject.NULL)
            put("motivo_consumo", closing.consumptionReason ?: JSONObject.NULL)
            put("atributo", closing.attribute ?: JSONObject.NULL)
        }
        val updatedClosing = api.updateHistoryClosing(
            id = "eq.$closingId",
            businessId = "eq.${closing.businessId}",
            body = closingPayload.asRequestBody(),
        )
        updatedClosing.use { response ->
            if (!response.hasExactlyOneRowWithId(closingId)) {
                throw IOException("Supabase no confirmó la actualización del cierre #$closingId.")
            }
        }

        val oldPayments = previous.payments.associateBy { it.paymentMethodId }
        closing.payments.forEach { payment ->
            val old = oldPayments[payment.paymentMethodId]
            when {
                old != null && old.amount.compareTo(payment.amount) == 0 -> Unit
                old != null -> {
                    val paymentPayload = JSONObject().apply { put("monto", payment.amount) }
                    val result = api.updateHistoryPayment(
                        closingId = "eq.$closingId",
                        methodId = "eq.${payment.paymentMethodId}",
                        body = paymentPayload.asRequestBody(),
                    )
                    result.use { response ->
                        if (!response.hasExactlyOneRow()) {
                            throw IOException("No se actualizó el pago del método #${payment.paymentMethodId}.")
                        }
                    }
                }
                else -> api.createPayments(
                    listOf(NewPaymentDto(
                        closingId = closingId,
                        paymentMethodId = payment.paymentMethodId,
                        amount = payment.amount,
                    ))
                )
            }
        }
        // Los métodos que no vienen en closing.payments se preservan; no se borran en lote.
        // Nota: modificar cierres + pagos por REST no es una transacción atómica.
    }

    override suspend fun delete(closingId: Long, businessId: Long) {
        val exists = getClosings(businessId).any { it.id == closingId }
        if (!exists) throw IllegalStateException("El cierre ya no existe en este negocio.")
        // La FK pagos.cierre_id debe estar configurada ON DELETE CASCADE.
        // Si no lo está, Supabase rechazará el DELETE sin borrar pagos.
        val deleted = api.deleteClosing(id = "eq.$closingId")
        if (deleted.singleOrNull()?.id != closingId) {
            throw IOException("Supabase no confirmó la eliminación del cierre #$closingId.")
        }
    }

    private suspend fun rollbackClosing(closingId: Long): Boolean = withContext(NonCancellable) {
        try {
            api.deleteClosing(id = "eq.$closingId").singleOrNull()?.id == closingId
        } catch (_: Exception) {
            false
        }
    }
}

private fun JSONObject.asRequestBody() =
    toString().toRequestBody("application/json; charset=utf-8".toMediaType())

private fun ResponseBody.hasExactlyOneRow(): Boolean =
    JSONArray(string()).length() == 1

private fun ResponseBody.hasExactlyOneRowWithId(id: Long): Boolean =
    JSONArray(string()).let { rows ->
        rows.length() == 1 && rows.getJSONObject(0).optLong("id", -1L) == id
    }
