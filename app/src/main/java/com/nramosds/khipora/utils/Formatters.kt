package com.nramosds.khipora.utils

import java.math.BigDecimal
import java.text.NumberFormat
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val peruLocale = Locale.forLanguageTag("es-PE")

fun formatCurrency(value: BigDecimal): String =
    "S/ " + NumberFormat.getNumberInstance(peruLocale).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(value)

fun todayIsoDate(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

fun isoDateFormatter() = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
    isLenient = false
    timeZone = TimeZone.getTimeZone("UTC")
}

fun formatDate(date: String, short: Boolean = false): String {
    val position = ParsePosition(0)
    val parsedDate = isoDateFormatter().parse(date, position)
    if (parsedDate == null || position.index != date.length) return "Fecha no disponible"
    return SimpleDateFormat(
        if (short) "dd MMM yyyy" else "d 'de' MMMM 'de' yyyy",
        peruLocale
    ).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(parsedDate)
}

fun formatTime(time: String?): String {
    if (time.isNullOrBlank()) return "Sin horario"
    return Regex("^([01][0-9]|2[0-3]):[0-5][0-9]").find(time)?.value ?: "Hora no disponible"
}

fun formatAudience(audience: String): String = when (audience) {
    "ninos" -> "Niños"
    "jovenes" -> "Jóvenes"
    "adultos" -> "Adultos"
    else -> audience.replaceFirstChar { it.titlecase(peruLocale) }
}
