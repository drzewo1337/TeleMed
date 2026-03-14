package com.example.myapplication.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.domain.model.Measurement
import com.example.myapplication.domain.model.MeasurementType
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.theme.BloodPressureBg
import com.example.myapplication.ui.theme.BloodPressureColor
import com.example.myapplication.ui.theme.BloodSugarBg
import com.example.myapplication.ui.theme.BloodSugarColor
import com.example.myapplication.ui.theme.TemperatureBg
import com.example.myapplication.ui.theme.TemperatureColor
import com.example.myapplication.ui.theme.WeightBg
import com.example.myapplication.ui.theme.WeightColor
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Kalendarz",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        MonthCalendar(
            currentMonth = uiState.currentMonth,
            selectedDate = uiState.selectedDate,
            daysWithMeasurements = uiState.daysWithMeasurements,
            handleSelectDate = viewModel::handleSelectDate,
            handleChangeMonth = viewModel::handleChangeMonth
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Pomiary: ${uiState.selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("pl")))}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.measurementsForSelectedDate.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Brak pomiarów w tym dniu",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.measurementsForSelectedDate) { measurement ->
                    MeasurementItem(
                        measurement = measurement,
                        onClick = { navController.navigate(Screen.EditMeasurement.createRoute(measurement.id)) },
                        onDelete = { viewModel.handleDelete(measurement) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthCalendar(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    daysWithMeasurements: Set<LocalDate>,
    handleSelectDate: (LocalDate) -> Unit,
    handleChangeMonth: (YearMonth) -> Unit
) {
    val monthFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale("pl"))

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { handleChangeMonth(currentMonth.minusMonths(1)) },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Poprzedni miesiąc",
                    modifier = Modifier.size(36.dp)
                )
            }
            Text(
                text = currentMonth.atDay(1).format(monthFormatter)
                    .replaceFirstChar { it.titlecase(Locale("pl")) },
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = { handleChangeMonth(currentMonth.plusMonths(1)) },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Następny miesiąc",
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val weekdayLabels = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            weekdayLabels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        val daysInMonth = currentMonth.lengthOfMonth()
        val firstDayOfMonth = currentMonth.atDay(1)
        val firstDayOfWeekIndex = firstDayOfMonth.dayOfWeek.value - 1

        val weeks: MutableList<List<LocalDate?>> = mutableListOf()
        var currentDay = 1

        while (currentDay <= daysInMonth) {
            val week: MutableList<LocalDate?> = mutableListOf()
            for (i in 0 until 7) {
                if (weeks.isEmpty() && i < firstDayOfWeekIndex) {
                    week.add(null)
                } else if (currentDay <= daysInMonth) {
                    week.add(currentMonth.atDay(currentDay))
                    currentDay++
                } else {
                    week.add(null)
                }
            }
            weeks.add(week)
        }

        weeks.forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                week.forEach { day ->
                    if (day == null) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        )
                    } else {
                        val hasMeasurement = daysWithMeasurements.contains(day)
                        val isSelected = day == selectedDate
                        val isToday = day == LocalDate.now()
                        val backgroundColor: Color = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            hasMeasurement -> MaterialTheme.colorScheme.secondaryContainer
                            else -> Color.Transparent
                        }
                        val textColor: Color = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            else -> MaterialTheme.colorScheme.onBackground
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(CircleShape)
                                .background(backgroundColor)
                                .clickable(
                                    role = Role.Button,
                                    onClickLabel = "Wybierz dzień ${day.dayOfMonth}"
                                ) { handleSelectDate(day) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = day.dayOfMonth.toString(),
                                    fontSize = 18.sp,
                                    fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal,
                                    color = textColor
                                )
                                if (hasMeasurement && !isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MeasurementItem(
    measurement: Measurement,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Usunąć pomiar?",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = "Czy na pewno chcesz usunąć ten pomiar? Tej operacji nie można cofnąć.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text(
                        text = "Usuń",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        text = "Anuluj",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        )
    }

    val (icon, bgColor, iconColor, typeName) = getMeasurementTypeVisuals(measurement.type)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClickLabel = "Edytuj pomiar $typeName"
            ) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = typeName,
                modifier = Modifier.size(40.dp),
                tint = iconColor
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = typeName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = getMeasurementValueText(measurement),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                measurement.note?.takeIf { it.isNotBlank() }?.let { note ->
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Usuń pomiar",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

private data class MeasurementVisuals(
    val icon: ImageVector,
    val backgroundColor: Color,
    val iconColor: Color,
    val typeName: String
)

private fun getMeasurementTypeVisuals(type: MeasurementType): MeasurementVisuals =
    when (type) {
        MeasurementType.TEMPERATURE -> MeasurementVisuals(
            Icons.Filled.DeviceThermostat, TemperatureBg, TemperatureColor, "Temperatura"
        )
        MeasurementType.BLOOD_PRESSURE -> MeasurementVisuals(
            Icons.Filled.Favorite, BloodPressureBg, BloodPressureColor, "Ciśnienie"
        )
        MeasurementType.BLOOD_SUGAR -> MeasurementVisuals(
            Icons.Filled.WaterDrop, BloodSugarBg, BloodSugarColor, "Cukier"
        )
        MeasurementType.WEIGHT -> MeasurementVisuals(
            Icons.Filled.MonitorWeight, WeightBg, WeightColor, "Masa ciała"
        )
    }

private fun getMeasurementValueText(measurement: Measurement): String =
    when (measurement.type) {
        MeasurementType.TEMPERATURE ->
            "${measurement.temperatureCelsius ?: "-"} °C"
        MeasurementType.BLOOD_PRESSURE ->
            "${measurement.systolicPressure ?: "-"} / ${measurement.diastolicPressure ?: "-"} mmHg"
        MeasurementType.BLOOD_SUGAR ->
            "${measurement.bloodSugarMgPerDl ?: "-"} mg/dL"
        MeasurementType.WEIGHT ->
            "${measurement.weightKg ?: "-"} kg"
    }
