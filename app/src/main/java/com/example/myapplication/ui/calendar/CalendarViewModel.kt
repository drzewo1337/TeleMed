package com.example.myapplication.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.MeasurementRepository
import com.example.myapplication.domain.model.Measurement
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val measurementsForSelectedDate: List<Measurement> = emptyList(),
    val daysWithMeasurements: Set<LocalDate> = emptySet()
)

class CalendarViewModel(
    private val measurementRepository: MeasurementRepository
) : ViewModel() {

    private val currentMonthState: MutableStateFlow<YearMonth> =
        MutableStateFlow(YearMonth.now())
    private val selectedDateState: MutableStateFlow<LocalDate> =
        MutableStateFlow(LocalDate.now())

    private val measurementsForSelectedDateFlow: StateFlow<List<Measurement>> =
        selectedDateState
            .flatMapLatest { date ->
                measurementRepository.observeMeasurementsForDay(date)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    private val daysWithMeasurementsFlow: StateFlow<Set<LocalDate>> =
        currentMonthState
            .flatMapLatest { month ->
                val start = month.atDay(1)
                val end = month.atEndOfMonth()
                measurementRepository.observeMeasurementsForRange(start, end)
            }
            .map { list ->
                list.map { it.timestamp.toLocalDate() }.toSet()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptySet()
            )

    val uiState: StateFlow<CalendarUiState> =
        combine(
            currentMonthState,
            selectedDateState,
            measurementsForSelectedDateFlow,
            daysWithMeasurementsFlow
        ) { currentMonth, selectedDate, dayMeasurements, daysWithMeasurements ->
            CalendarUiState(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                measurementsForSelectedDate = dayMeasurements,
                daysWithMeasurements = daysWithMeasurements
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CalendarUiState()
        )

    fun handleSelectDate(date: LocalDate) {
        selectedDateState.value = date
        currentMonthState.value = YearMonth.from(date)
    }

    fun handleChangeMonth(month: YearMonth) {
        currentMonthState.value = month
        val selected = selectedDateState.value
        if (YearMonth.from(selected) != month) {
            selectedDateState.value = month.atDay(1)
        }
    }

    fun handleDelete(measurement: Measurement) {
        viewModelScope.launch {
            measurementRepository.deleteMeasurement(measurement)
        }
    }
}
