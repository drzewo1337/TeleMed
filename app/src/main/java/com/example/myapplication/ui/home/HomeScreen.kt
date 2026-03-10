package com.example.myapplication.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.domain.model.Measurement
import com.example.myapplication.domain.model.MeasurementType
import com.example.myapplication.ui.navigation.Screen
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Dzienniczek zdrowia seniora",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        MonthCalendar(
            currentMonth = uiState.currentMonth,
            selectedDate = uiState.selectedDate,
            daysWithMeasurements = uiState.daysWithMeasurements,
            handleSelectDate = viewModel::handleSelectDate,
            handleChangeMonth = viewModel::handleChangeMonth
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { navController.navigate(Screen.AddMeasurement.createRoute(uiState.selectedDate.toString())) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Dodaj pomiar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pomiary dla dnia ${uiState.selectedDate}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
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
            Button(onClick = { handleChangeMonth(currentMonth.minusMonths(1)) }) {
                Text(text = "<")
            }
            Text(
                text = currentMonth.atDay(1).format(monthFormatter),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Button(onClick = { handleChangeMonth(currentMonth.plusMonths(1)) }) {
                Text(text = ">")
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

        val weekdayLabels = listOf("P", "W", "Ś", "C", "P", "S", "N")
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
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
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
                                .height(32.dp)
                        )
                    } else {
                        val hasMeasurement = daysWithMeasurements.contains(day)
                        val isSelected = day == selectedDate
                        val backgroundColor: Color =
                            when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                hasMeasurement -> MaterialTheme.colorScheme.secondaryContainer
                                else -> Color.Transparent
                            }
                        val textColor: Color =
                            if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onBackground
                            }
                        Text(
                            text = day.dayOfMonth.toString(),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .background(backgroundColor)
                                .clickable { handleSelectDate(day) },
                            textAlign = TextAlign.Center,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MeasurementItem(
    measurement: Measurement,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when (measurement.type) {
                    MeasurementType.TEMPERATURE -> "Temperatura"
                    MeasurementType.BLOOD_PRESSURE -> "Ciśnienie"
                    MeasurementType.BLOOD_SUGAR -> "Cukier"
                    MeasurementType.WEIGHT -> "Masa ciała"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            val valueText = when (measurement.type) {
                MeasurementType.TEMPERATURE ->
                    "${measurement.temperatureCelsius ?: "-"} °C"

                MeasurementType.BLOOD_PRESSURE ->
                    "${measurement.systolicPressure ?: "-"} / ${measurement.diastolicPressure ?: "-"} mmHg"

                MeasurementType.BLOOD_SUGAR ->
                    "${measurement.bloodSugarMgPerDl ?: "-"} mg/dL"

                MeasurementType.WEIGHT ->
                    "${measurement.weightKg ?: "-"} kg"
            }

            Text(text = valueText)

            measurement.note?.takeIf { it.isNotBlank() }?.let { note ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = note, style = MaterialTheme.typography.bodySmall)
            }
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.padding(start = 8.dp),
                content = {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Usuń pomiar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
}
