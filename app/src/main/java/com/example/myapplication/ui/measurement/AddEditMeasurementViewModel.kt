package com.example.myapplication.ui.measurement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.MeasurementRepository
import com.example.myapplication.data.repository.SettingsRepository
import com.example.myapplication.domain.anomaly.MeasurementAlertService
import com.example.myapplication.domain.model.Measurement
import com.example.myapplication.domain.model.MeasurementType
import com.example.myapplication.domain.model.UserSettings
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AddEditMeasurementUiState(
    val selectedType: MeasurementType = MeasurementType.TEMPERATURE,
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val temperatureInput: String = "",
    val systolicInput: String = "",
    val diastolicInput: String = "",
    val sugarInput: String = "",
    val weightInput: String = "",
    val note: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val userSettings: UserSettings = UserSettings()
)

class AddEditMeasurementViewModel(
    private val measurementRepository: MeasurementRepository,
    settingsRepository: SettingsRepository,
    private val measurementAlertService: MeasurementAlertService,
    private val measurementId: Long?
) : ViewModel() {

    private val internalState = MutableStateFlow(AddEditMeasurementUiState())

    val uiState: StateFlow<AddEditMeasurementUiState> =
        internalState.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AddEditMeasurementUiState()
        )

    init {
        viewModelScope.launch {
            val settings = settingsRepository.getUserSettings()
            internalState.value = internalState.value.copy(userSettings = settings)

            if (measurementId != null) {
                val existing = measurementRepository.getMeasurementById(measurementId)
                if (existing != null) {
                    internalState.value = internalState.value.copy(
                        selectedType = existing.type,
                        date = existing.timestamp.toLocalDate(),
                        time = existing.timestamp.toLocalTime(),
                        temperatureInput = existing.temperatureCelsius?.toString() ?: "",
                        systolicInput = existing.systolicPressure?.toString() ?: "",
                        diastolicInput = existing.diastolicPressure?.toString() ?: "",
                        sugarInput = existing.bloodSugarMgPerDl?.toString() ?: "",
                        weightInput = existing.weightKg?.toString() ?: "",
                        note = existing.note.orEmpty()
                    )
                }
            }
        }
    }

    fun handleTypeChange(type: MeasurementType) {
        internalState.value = internalState.value.copy(selectedType = type, errorMessage = null)
    }

    fun handleDateChange(date: LocalDate) {
        internalState.value = internalState.value.copy(date = date, errorMessage = null)
    }

    fun handleTimeChange(time: LocalTime) {
        internalState.value = internalState.value.copy(time = time, errorMessage = null)
    }

    fun handleTemperatureChange(value: String) {
        internalState.value = internalState.value.copy(temperatureInput = value, errorMessage = null)
    }

    fun handleSystolicChange(value: String) {
        internalState.value = internalState.value.copy(systolicInput = value, errorMessage = null)
    }

    fun handleDiastolicChange(value: String) {
        internalState.value = internalState.value.copy(diastolicInput = value, errorMessage = null)
    }

    fun handleSugarChange(value: String) {
        internalState.value = internalState.value.copy(sugarInput = value, errorMessage = null)
    }

    fun handleWeightChange(value: String) {
        internalState.value = internalState.value.copy(weightInput = value, errorMessage = null)
    }

    fun handleNoteChange(value: String) {
        internalState.value = internalState.value.copy(note = value)
    }

    fun handleSave(onSuccess: () -> Unit) {
        val state = internalState.value
        val timestamp = LocalDateTime.of(state.date, state.time)

        val measurement = when (state.selectedType) {
            MeasurementType.TEMPERATURE -> {
                val temperature = state.temperatureInput.toDoubleOrNull()
                if (temperature == null) {
                    internalState.value =
                        state.copy(errorMessage = "Podaj poprawną temperaturę.")
                    return
                }
                Measurement(
                    id = measurementId ?: 0L,
                    timestamp = timestamp,
                    type = MeasurementType.TEMPERATURE,
                    temperatureCelsius = temperature,
                    note = state.note.ifBlank { null }
                )
            }

            MeasurementType.BLOOD_PRESSURE -> {
                val systolic = state.systolicInput.toIntOrNull()
                val diastolic = state.diastolicInput.toIntOrNull()
                if (systolic == null || diastolic == null) {
                    internalState.value =
                        state.copy(errorMessage = "Podaj poprawne wartości ciśnienia.")
                    return
                }
                Measurement(
                    id = measurementId ?: 0L,
                    timestamp = timestamp,
                    type = MeasurementType.BLOOD_PRESSURE,
                    systolicPressure = systolic,
                    diastolicPressure = diastolic,
                    note = state.note.ifBlank { null }
                )
            }

            MeasurementType.BLOOD_SUGAR -> {
                val sugar = state.sugarInput.toDoubleOrNull()
                if (sugar == null) {
                    internalState.value =
                        state.copy(errorMessage = "Podaj poprawny poziom cukru.")
                    return
                }
                Measurement(
                    id = measurementId ?: 0L,
                    timestamp = timestamp,
                    type = MeasurementType.BLOOD_SUGAR,
                    bloodSugarMgPerDl = sugar,
                    note = state.note.ifBlank { null }
                )
            }

            MeasurementType.WEIGHT -> {
                val weight = state.weightInput.toDoubleOrNull()
                if (weight == null) {
                    internalState.value =
                        state.copy(errorMessage = "Podaj poprawną masę ciała.")
                    return
                }
                Measurement(
                    id = measurementId ?: 0L,
                    timestamp = timestamp,
                    type = MeasurementType.WEIGHT,
                    weightKg = weight,
                    note = state.note.ifBlank { null }
                )
            }
        }

        internalState.value = state.copy(isSaving = true, errorMessage = null)

        viewModelScope.launch {
            try {
                measurementRepository.saveMeasurement(measurement)
                measurementAlertService.handleNewMeasurement(measurement)
                onSuccess()
            } catch (t: Throwable) {
                internalState.value =
                    state.copy(isSaving = false, errorMessage = "Nie udało się zapisać pomiaru.")
            }
        }
    }
}

