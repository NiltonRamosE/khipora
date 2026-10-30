package com.nramosds.khipora.presentation.statistics

import com.nramosds.khipora.domain.model.DailyClosing
import com.nramosds.khipora.domain.model.PaymentMethod
import java.math.BigDecimal
import java.math.RoundingMode

internal data class StatisticsSummary(
    val total: BigDecimal,
    val customers: Long,
    val units: Long,
    val days: Int,
    val averagePerCustomer: BigDecimal,
    val dailyAverage: BigDecimal,
    val paymentMethods: List<PaymentMethodIncome>,
    val dailyIncome: List<DailyIncome>,
) {
    companion object {
        fun calculate(
            closings: List<DailyClosing>,
            paymentMethods: List<PaymentMethod>
        ): StatisticsSummary {
            val dailyIncome = closings.groupBy { it.date }.map { (date, records) ->
                DailyIncome(date, records.fold(BigDecimal.ZERO) { acc, c -> acc + c.total })
            }.sortedByDescending { it.date }
            val total = dailyIncome.fold(BigDecimal.ZERO) { acc, day -> acc + day.amount }
            val customers = closings.sumOf { it.customers.toLong() }
            val methodNames = paymentMethods.associate { it.id to it.name }
            val incomeByMethod = closings.flatMap { it.payments }.groupBy { it.paymentMethodId }
                .map { (id, payments) ->
                    val amount =
                        payments.fold(BigDecimal.ZERO) { acc, payment -> acc + payment.amount }
                    PaymentMethodIncome(
                        methodNames[id] ?: "Método #$id", amount,
                        if (total.signum() == 0) 0f else amount.divide(
                            total,
                            4,
                            RoundingMode.HALF_UP
                        ).toFloat().coerceIn(0f, 1f),
                    )
                }.sortedByDescending { it.amount }

            fun average(divisor: Long) = if (divisor == 0L) BigDecimal.ZERO else total.divide(
                divisor.toBigDecimal(),
                2,
                RoundingMode.HALF_UP
            )
            return StatisticsSummary(
                total, customers, closings.sumOf { it.units.toLong() }, dailyIncome.size,
                average(customers), average(dailyIncome.size.toLong()), incomeByMethod, dailyIncome
            )
        }
    }
}
