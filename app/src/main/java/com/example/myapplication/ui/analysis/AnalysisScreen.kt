package com.example.myapplication.ui.analysis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter

private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalysisScreen(viewModel: AnalysisViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Szczegółowa analiza",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Zakres czasu:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChartTimeRange.entries.forEach { range ->
                FilterChip(
                    selected = uiState.timeRange == range,
                    onClick = { viewModel.setTimeRange(range) },
                    label = {
                        Text(
                            text = range.label,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier.height(48.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .height(280.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .pointerInput(Unit) {
                    detectTapGestures { }
                }
        ) {
            ChartContent(
                series = uiState.series.filter { it.visible && it.points.isNotEmpty() },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Legenda (dotknij, aby włączyć/wyłączyć):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.series.forEach { series ->
                val isVisible = uiState.seriesVisibility[series.id] == true
                Surface(
                    modifier = Modifier
                        .clickable { viewModel.toggleSeries(series.id) }
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isVisible) Color(series.color).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(if (isVisible) Color(series.color) else Color.Gray)
                        )
                        Text(
                            text = series.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (isVisible) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        uiState.selectedPoint?.let { (dateTime, values) ->
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = dateTime.format(dateTimeFormatter),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    values.forEach { (id, value) ->
                        Text(
                            text = "${id.name}: $value",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
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
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val allPoints = series.flatMap { it.points }
    val minTime = allPoints.minOf { it.first }
    val maxTime = allPoints.maxOf { it.first }
    val timeRange = (maxTime - minTime).coerceAtLeast(1L)

    Canvas(modifier = modifier.padding(8.dp)) {
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
                style = Stroke(width = 4f)
            )
            s.points.forEach { (t, v) ->
                drawCircle(
                    color = Color(s.color),
                    radius = 6f,
                    center = Offset(timeToX(t), valueToY(v))
                )
            }
        }
    }
}
