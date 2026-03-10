package com.example.myapplication.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

@Composable
fun ChartsScreen(viewModel: ChartsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Wykresy pomiarów",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Zakres czasu", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ChartTimeRange.entries.forEach { range ->
                FilterChip(
                    selected = uiState.timeRange == range,
                    onClick = { viewModel.setTimeRange(range) },
                    label = { Text(range.label) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.setTimeRange(ChartTimeRange.MONTH) },
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text("Dzisiaj (miesiąc)")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // optional: find nearest point for tooltip
                    }
                }
        ) {
            ChartContent(
                series = uiState.series.filter { it.visible && it.points.isNotEmpty() },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Legenda (dotknij, aby włączyć/wyłączyć)", style = MaterialTheme.typography.labelSmall)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            uiState.series.forEach { series ->
                val isVisible = uiState.seriesVisibility[series.id] == true
                Surface(
                    modifier = Modifier
                        .clickable { viewModel.toggleSeries(series.id) }
                        .padding(4.dp),
                    shape = MaterialTheme.shapes.small,
                    color = if (isVisible) Color(series.color).copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(if (isVisible) Color(series.color) else Color.Gray)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = series.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isVisible) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        uiState.selectedPoint?.let { (dateTime, values) ->
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = dateTime.format(dateTimeFormatter),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    values.forEach { (id, value) ->
                        Text(
                            text = "${id.name}: $value",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartContent(
    series: List<ChartSeries>,
    modifier: Modifier = Modifier
) {
    if (series.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Brak danych w wybranym zakresie",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val allPoints = series.flatMap { it.points }
    val minTime = allPoints.minOf { it.first }
    val maxTime = allPoints.maxOf { it.first }
    val timeRange = (maxTime - minTime).coerceAtLeast(1L)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val paddingHorizontal = 40f
        val paddingVertical = 24f
        val chartWidth = width - 2 * paddingHorizontal
        val chartHeight = height - 2 * paddingVertical

        fun timeToX(t: Long): Float {
            return paddingHorizontal + ((t - minTime).toFloat() / timeRange * chartWidth)
        }

        series.forEach { s ->
            if (s.points.isEmpty()) return@forEach
            val minVal = s.points.minOf { it.second }
            val maxVal = s.points.maxOf { it.second }
            val valueRange = (maxVal - minVal).coerceAtLeast(0.01)
            fun valueToY(v: Double): Float {
                return (paddingVertical + chartHeight - ((v - minVal) / valueRange * chartHeight)).toFloat()
            }
            val path = Path()
            val first = s.points.first()
            path.moveTo(timeToX(first.first), valueToY(first.second))
            s.points.drop(1).forEach { (t, v) ->
                path.lineTo(timeToX(t), valueToY(v))
            }
            drawPath(
                path = path,
                color = Color(s.color),
                style = Stroke(width = 3f)
            )
            s.points.forEach { (t, v) ->
                drawCircle(
                    color = Color(s.color),
                    radius = 4f,
                    center = Offset(timeToX(t), valueToY(v))
                )
            }
        }
    }
}
