package com.nramosds.khipora.presentation.statistics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nramosds.khipora.domain.model.DailyClosing
import com.nramosds.khipora.presentation.statistics.DailyIncome
import com.nramosds.khipora.utils.components.AppCard
import com.nramosds.khipora.utils.formatCurrency
import com.nramosds.khipora.utils.formatDate
import kotlin.math.roundToInt

/** Tendencia de los ultimos 7 DIAS CON REGISTRO; no supone ventas cero en los dias faltantes. */
@Composable
internal fun IncomeLineChartCard(days: List<DailyIncome>) {
    val ordered = remember(days) { days.sortedBy { it.date }.takeLast(7) }
    val colors = MaterialTheme.colorScheme
    var selectedIndex by remember(ordered) { mutableIntStateOf((ordered.size - 1).coerceAtLeast(0)) }

    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Outlined.Insights, contentDescription = null, tint = colors.primary)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Evolución de ingresos", style = MaterialTheme.typography.titleLarge)
                Text("Últimos ${ordered.size} días con registro", style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant)
            }
        }

        if (ordered.isEmpty()) {
            Text("Aún no hay ingresos para mostrar.", color = colors.onSurfaceVariant)
        } else {
            val selected = ordered[selectedIndex.coerceIn(ordered.indices)]
            val maxValue = (ordered.maxOfOrNull { it.amount.toFloat() } ?: 0f).coerceAtLeast(1f)
            val primary = colors.primary
            val guide = colors.outlineVariant
            val selectedPoint = colors.tertiary

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("DÍA SELECCIONADO", style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant)
                    Text(formatDate(selected.date), style = MaterialTheme.typography.bodyMedium)
                }
                Text(formatCurrency(selected.amount), style = MaterialTheme.typography.titleLarge,
                    color = colors.primary, fontWeight = FontWeight.Bold)
            }

            Canvas(
                modifier = Modifier.fillMaxWidth().height(185.dp).pointerInput(ordered) {
                    detectTapGestures { tap ->
                        selectedIndex = if (ordered.size == 1) 0 else {
                            val left = 12.dp.toPx()
                            val right = size.width - left
                            val position = ((tap.x - left) / (right - left).coerceAtLeast(1f))
                                .coerceIn(0f, 1f)
                            (position * (ordered.size - 1)).roundToInt()
                        }
                    }
                }
            ) {
                val left = 12.dp.toPx()
                val right = size.width - left
                val top = 12.dp.toPx()
                val bottom = size.height - 12.dp.toPx()
                val h = bottom - top
                val width = right - left

                repeat(4) { index ->
                    val y = top + h * index / 3f
                    drawLine(guide.copy(alpha = 0.70f), Offset(left, y), Offset(right, y),
                        strokeWidth = 1.dp.toPx())
                }
                val points = ordered.mapIndexed { index, day ->
                    val x = if (ordered.size == 1) (left + right) / 2f
                        else left + width * index / (ordered.size - 1).toFloat()
                    val fraction = (day.amount.toFloat() / maxValue).coerceIn(0f, 1f)
                    Offset(x, bottom - fraction * h)
                }
                if (points.size >= 2) {
                    val area = Path().apply {
                        moveTo(points.first().x, bottom)
                        lineTo(points.first().x, points.first().y)
                        points.drop(1).forEach { lineTo(it.x, it.y) }
                        lineTo(points.last().x, bottom)
                        close()
                    }
                    drawPath(area, brush = Brush.verticalGradient(
                        listOf(primary.copy(alpha = 0.25f), primary.copy(alpha = 0.02f)),
                        startY = top, endY = bottom))
                    val line = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        points.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(line, color = primary, style = Stroke(width = 3.dp.toPx(),
                        cap = StrokeCap.Round))
                }
                points.forEachIndexed { index, point ->
                    drawCircle(color = if (index == selectedIndex) selectedPoint else primary,
                        radius = if (index == selectedIndex) 6.dp.toPx() else 4.dp.toPx(),
                        center = point)
                    drawCircle(color = colors.surface, radius = 2.dp.toPx(), center = point)
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(shortIsoDate(ordered.first().date), style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant)
                if (ordered.size > 2) {
                    Text(shortIsoDate(ordered[ordered.size / 2].date),
                        style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                }
                if (ordered.size > 1) {
                    Text(shortIsoDate(ordered.last().date),
                        style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                }
            }
            Text("Toca un punto del gráfico para consultar su ingreso.",
                style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}

private fun shortIsoDate(iso: String): String =
    if (iso.length >= 10) "${iso.substring(8, 10)}/${iso.substring(5, 7)}" else iso

private data class AudienceSlice(val title: String, val count: Int, val color: Color)

/** Las proporciones se refieren a CIERRES, no al numero de clientes individuales. */
@Composable
internal fun AudienceDonutChartCard(closings: List<DailyClosing>) {
    val colors = MaterialTheme.colorScheme
    val known = setOf("ninos", "jovenes", "adultos")
    val slices = listOf(
        AudienceSlice("Niños", closings.count { it.audience == "ninos" }, colors.primary),
        AudienceSlice("Jóvenes", closings.count { it.audience == "jovenes" }, colors.tertiary),
        AudienceSlice("Adultos", closings.count { it.audience == "adultos" }, colors.secondary),
        AudienceSlice("Sin registrar", closings.count { it.audience !in known }, colors.outline),
    ).filter { it.count > 0 }
    val total = closings.size

    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Outlined.PeopleOutline, null, tint = colors.primary)
            Column {
                Text("Tu público predominante", style = MaterialTheme.typography.titleLarge)
                Text("Distribución por cierres registrados", style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant)
            }
        }
        if (total == 0) {
            Text("Sin datos para mostrar.", color = colors.onSurfaceVariant)
        } else {
            Box(Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center) {
                Box(Modifier.size(192.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(192.dp)) {
                        val thickness = 26.dp.toPx()
                        val inset = thickness / 2f + 2.dp.toPx()
                        val arcTopLeft = Offset(inset, inset)
                        val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
                        drawArc(color = guideColor(colors.outlineVariant), startAngle = 0f,
                            sweepAngle = 360f, useCenter = false, topLeft = arcTopLeft, size = arcSize,
                            style = Stroke(width = thickness))
                        var start = -90f
                        slices.forEach { slice ->
                            val sweep = slice.count.toFloat() / total * 360f
                            val gap = if (sweep > 4f) 1.2f else 0f
                            drawArc(color = slice.color, startAngle = start + gap,
                                sweepAngle = (sweep - 2 * gap).coerceAtLeast(0f),
                                useCenter = false, topLeft = arcTopLeft, size = arcSize,
                                style = Stroke(width = thickness,
                                    cap = StrokeCap.Butt))
                            start += sweep
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(total.toString(), style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold)
                        Text(if (total == 1) "cierre" else "cierres",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant)
                    }
                }
            }
            slices.forEach { slice ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(11.dp).background(slice.color, CircleShape))
                    Text(slice.title, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text("${slice.count}", style = MaterialTheme.typography.labelLarge)
                    Text("${(slice.count * 100f / total).roundToInt()} %",
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            Text("Cada cierre indica un solo público predominante; estos porcentajes no " +
                "representan el porcentaje real de clientes por edad.",
                style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}

private fun guideColor(color: Color): Color = color.copy(alpha = 0.55f)

private data class InsightCategory(val code: String, val label: String)

@Composable
private fun InsightBars(
    title: String,
    choices: List<InsightCategory>,
    values: List<String?>,
    barColor: Color,
) {
    val colors = MaterialTheme.colorScheme
    val validValues = values.filter { value -> choices.any { it.code == value } }
    val registered = validValues.size
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text("$registered de ${values.size} cierres con respuesta",
            style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        if (registered == 0) {
            Text("Aún no hay respuestas para este indicador.",
                style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        } else {
            choices.forEach { choice ->
                val count = validValues.count { it == choice.code }
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(choice.label, style = MaterialTheme.typography.bodyMedium)
                        Text("$count · ${(count * 100f / registered).roundToInt()} %",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant)
                    }
                    Box(Modifier.fillMaxWidth().height(9.dp)
                        .background(colors.surfaceVariant, RoundedCornerShape(100))) {
                        Box(Modifier.fillMaxWidth(count.toFloat() / registered)
                            .height(9.dp).background(barColor, RoundedCornerShape(100)))
                    }
                }
            }
        }
    }
}
