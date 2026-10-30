package com.nramosds.khipora.presentation.closing

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import com.nramosds.khipora.domain.model.NewDailyClosing
import com.nramosds.khipora.domain.model.Payment
import com.nramosds.khipora.domain.model.PaymentMethod
import com.nramosds.khipora.utils.formatDate
import com.nramosds.khipora.utils.todayIsoDate
import java.math.BigDecimal

internal fun parseAmount(value: String): BigDecimal? = value.replace(',', '.').toBigDecimalOrNull()

@Stable
internal class ClosingFormState {
    var date by mutableStateOf(todayIsoDate())
    var units by mutableStateOf("")
    var customers by mutableStateOf("")
    var startTime by mutableStateOf("")
    var endTime by mutableStateOf("")
    var audience by mutableStateOf("adultos")
    var notes by mutableStateOf("")
    var amounts by mutableStateOf(emptyMap<Long, String>())
    var showValidation by mutableStateOf(false)
    var consumptionReason by mutableStateOf("")
    var attribute by mutableStateOf("")
    val formattedDate get() = formatDate(date)
    val hasUnitsError get() = showValidation && (units.toIntOrNull()?.let { it >= 0 } != true)
    val hasCustomersError get() = showValidation && (customers.toIntOrNull()?.let { it >= 0 } != true)
    fun selectSchedule(start: String, end: String) {
        startTime = start
        endTime = end
    }
    val hasValidHours: Boolean get() =
            (startTime.isBlank() && endTime.isBlank()) ||
                listOf(
                    "09:00" to "12:00",
                    "12:00" to "15:00",
                    "15:00" to "17:00"
                ).any { (start, end) ->
                    startTime == start && endTime == end
                }
    fun total(paymentMethods: List<PaymentMethod>): BigDecimal =
        paymentMethods.fold(BigDecimal.ZERO) { acc, paymentMethod ->
            acc + (parseAmount(amounts[paymentMethod.id].orEmpty()) ?: BigDecimal.ZERO)
        }

    fun createClosing(businessId: Long, paymentMethods: List<PaymentMethod>): NewDailyClosing? {
        showValidation = true
        val unitCount = units.toIntOrNull() ?: return null
        val customerCount = customers.toIntOrNull() ?: return null
        if (unitCount < 0 || customerCount < 0 || !hasValidHours || paymentMethods.isEmpty()) return null
        val payments = paymentMethods.map { paymentMethod ->
            val amount = parseAmount(amounts[paymentMethod.id].orEmpty()) ?: return null
            if (amount.signum() < 0 || amount.scale() > 2) return null
            Payment(paymentMethod.id, amount)
        }
        return NewDailyClosing(
            businessId = businessId,
            date = date,
            units = unitCount,
            customers = customerCount,
            startTime = startTime.ifBlank { null },
            endTime = endTime.ifBlank { null },
            audience = audience,
            notes = notes.ifBlank { null },
            payments = payments,
            consumptionReason = consumptionReason.ifBlank { null },
            attribute = attribute.ifBlank { null },
        )
    }

    companion object {
        private fun isValidTime(value: String) = Regex("([01][0-9]|2[0-3]):[0-5][0-9]").matches(value)

        val Saver = listSaver<ClosingFormState, String>(
            save = { form ->
                listOf(
                    form.date,
                    form.units,
                    form.customers,
                    form.startTime,
                    form.endTime,
                    form.audience,
                    form.notes,
                    form.showValidation.toString(),
                    form.consumptionReason,
                    form.attribute,
                ) + form.amounts.flatMap { (id, amount) ->
                    listOf(id.toString(), amount)
                }
            },
            restore = { values ->
                ClosingFormState().apply {
                    date = values[0]
                    units = values[1]
                    customers = values[2]
                    startTime = values[3]
                    endTime = values[4]
                    audience = values[5]
                    notes = values[6]
                    showValidation = values[7].toBoolean()
                    consumptionReason = values[8]
                    attribute = values[9]

                    amounts = values.drop(10)
                        .chunked(2)
                        .associate { pair ->
                            pair[0].toLong() to pair[1]
                        }
                }
            }
        )
    }
}
