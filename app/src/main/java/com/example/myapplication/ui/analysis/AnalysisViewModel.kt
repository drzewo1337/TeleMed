package com.example.myapplication.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.MeasurementRepository
import com.example.myapplication.data.repository.SettingsRepository
import com.example.myapplication.domain.model.Measurement
import com.example.myapplication.domain.model.MeasurementType
import com.example.myapplication.domain.model.UserSettings
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

enum class ChartTimeRange(val label: String, val days: Int) {
    WEEK("Tydzień", 7),
    MONTH("Miesiąc", 30),
    THREE_MONTHS("3 miesiące", 90),
    YEAR("Rok", 365)
}

data class ChartSeries(
    val id: ChartSeriesId,
    val label: String,
    val points: List<Pair<Long, Double>>,
    val visible: Boolean,
    val color: Long
)

enum class ChartSeriesId { TEMPERATURE, SYSTOLIC, DIASTOLIC, BLOOD_SUGAR, WEIGHT }

data class AnalysisUiState(
    val timeRange: ChartTimeRange = ChartTimeRange.MONTH,
    val seriesVisibility: Map<ChartSeriesId, Boolean> = ChartSeriesId.entries.associateWith { true },
    val series: List<ChartSeries> = emptyList(),
    val allMeasurements: List<Measurement> = emptyList(),
    val settings: UserSettings = UserSettings(),
    val selectedPoint: Pair<LocalDateTime, Map<ChartSeriesId, Double>>? = null
)

class AnalysisViewModel(
    private val measurementRepository: MeasurementRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val timeRangeState = MutableStateFlow(ChartTimeRange.MONTH)
    private val visibilityState = MutableStateFlow(
        ChartSeriesId.entries.associateWith { true }
    )
    private val selectedPointState = MutableStateFlow<Pair<LocalDateTime, Map<ChartSeriesId, Double>>?>(null)

    private val rangeFlow = timeRangeState.flatMapLatest { range ->
        val end = LocalDate.now()
        val start = end.minusDays(range.days.toLong())
        measurementRepository.observeMeasurementsForRange(start, end)
    }

    private val settingsFlow = settingsRepository.observeUserSettings()

    val uiState: StateFlow<AnalysisUiState> = combine(
        timeRangeState,
        visibilityState,
        rangeFlow,
        settingsFlow,
        selectedPointState
    ) { timeRange, visibility, measurements, settings, selectedPoint ->
        val pointsBySeries = buildSeriesPoints(measurements)
        val series = listOf(
            ChartSeries(
                id = ChartSeriesId.TEMPERATURE,
                label = "Temperatura °C",
                points = pointsBySeries.temperature,
                visible = visibility[ChartSeriesId.TEMPERATURE] == true,
                color = 0xFFE53935
            ),
            ChartSeries(
                id = ChartSeriesId.SYSTOLIC,
                label = "Skurczowe mmHg",
                points = pointsBySeries.systolic,
                visible = visibility[ChartSeriesId.SYSTOLIC] == true,
                color = 0xFF1E88E5
            ),
            ChartSeries(
                id = ChartSeriesId.DIASTOLIC,
                label = "Rozkurczowe mmHg",
                points = pointsBySeries.diastolic,
                visible = visibility[ChartSeriesId.DIASTOLIC] == true,
                color = 0xFF43A047
            ),
            ChartSeries(
                id = ChartSeriesId.BLOOD_SUGAR,
                label = "Cukier mg/dL",
                points = pointsBySeries.bloodSugar,
                visible = visibility[ChartSeriesId.BLOOD_SUGAR] == true,
                color = 0xFFFB8C00
            ),
            ChartSeries(
                id = ChartSeriesId.WEIGHT,
                label = "Masa kg",
                points = pointsBySeries.weight,
                visible = visibility[ChartSeriesId.WEIGHT] == true,
                color = 0xFF8E24AA
            )
        )
        AnalysisUiState(
            timeRange = timeRange,
            seriesVisibility = visibility,
            series = series,
            allMeasurements = measurements,
            settings = settings,
            selectedPoint = selectedPoint
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AnalysisUiState()
    )

    private data class SeriesPoints(
        val temperature: List<Pair<Long, Double>>,
        val systolic: List<Pair<Long, Double>>,
        val diastolic: List<Pair<Long, Double>>,
        val bloodSugar: List<Pair<Long, Double>>,
        val weight: List<Pair<Long, Double>>
    )

    private fun buildSeriesPoints(measurements: List<Measurement>): SeriesPoints {
        val temp = measurements
            .filter { it.type == MeasurementType.TEMPERATURE && it.temperatureCelsius != null }
            .map { it.timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() to it.temperatureCelsius!! }
            .sortedBy { it.first }
        val sys = measurements
            .filter { it.type == MeasurementType.BLOOD_PRESSURE && it.systolicPressure != null }
            .map { it.timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() to it.systolicPressure!!.toDouble() }
            .sortedBy { it.first }
        val dia = measurements
            .filter { it.type == MeasurementType.BLOOD_PRESSURE && it.diastolicPressure != null }
            .map { it.timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() to it.diastolicPressure!!.toDouble() }
            .sortedBy { it.first }
        val sugar = measurements
            .filter { it.type == MeasurementType.BLOOD_SUGAR && it.bloodSugarMgPerDl != null }
            .map { it.timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() to it.bloodSugarMgPerDl!! }
            .sortedBy { it.first }
        val weight = measurements
            .filter { it.type == MeasurementType.WEIGHT && it.weightKg != null }
            .map { it.timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() to it.weightKg!! }
            .sortedBy { it.first }
        return SeriesPoints(temp, sys, dia, sugar, weight)
    }

    fun setTimeRange(range: ChartTimeRange) {
        timeRangeState.value = range
    }

    fun toggleSeries(seriesId: ChartSeriesId) {
        visibilityState.value = visibilityState.value.toMutableMap().apply {
            put(seriesId, !(this[seriesId] ?: true))
        }
    }

    fun setSelectedPoint(point: Pair<LocalDateTime, Map<ChartSeriesId, Double>>?) {
        selectedPointState.value = point
    }
}
