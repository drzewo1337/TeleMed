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
    val userSettings: UserSettings = UserSettings(),
    /** W trybie edycji: typ edytowanego pomiaru (pokazujemy wszystkie wiersze, zapisujemy tylko ten). */
    val editType: MeasurementType? = null,
    val editMeasurementId: Long? = null,
    val temperatureError: String? = null,
    val systolicError: String? = null,
    val diastolicError: String? = null,
    val sugarError: String? = null,
    val weightError: String? = null
)

class AddEditMeasurementViewModel(
    private val measurementRepository: MeasurementRepository,
    settingsRepository: SettingsRepository,
    private val measurementAlertService: MeasurementAlertService,
    private val measurementId: Long?,
    initialSelectedDate: String? = null
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
            var state = internalState.value.copy(userSettings = settings)

            if (initialSelectedDate != null) {
                try {
                    state = state.copy(date = LocalDate.parse(initialSelectedDate))
                } catch (_: Exception) { }
            }

            if (measurementId != null) {
                val existing = measurementRepository.getMeasurementById(measurementId)
                if (existing != null) {
                    state = state.copy(
                        editType = existing.type,
                        editMeasurementId = measurementId,
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
            internalState.value = state
        }
    }

    fun handleDateChange(date: LocalDate) {
        internalState.value = internalState.value.copy(date = date, errorMessage = null)
    }

    fun handleTimeChange(time: LocalTime) {
        internalState.value = internalState.value.copy(time = time, errorMessage = null)
    }

    fun handleTemperatureChange(value: String) {
        internalState.value = internalState.value.copy(
            temperatureInput = value,
            errorMessage = null,
            temperatureError = validateTemperature(value)
        )
    }

    fun handleSystolicChange(value: String) {
        internalState.value = internalState.value.copy(
            systolicInput = value,
            errorMessage = null,
            systolicError = validateSystolic(value)
        )
    }

    fun handleDiastolicChange(value: String) {
        internalState.value = internalState.value.copy(
            diastolicInput = value,
            errorMessage = null,
            diastolicError = validateDiastolic(value)
        )
    }

    fun handleSugarChange(value: String) {
        internalState.value = internalState.value.copy(
            sugarInput = value,
            errorMessage = null,
            sugarError = validateSugar(value)
        )
    }

    fun handleWeightChange(value: String) {
        internalState.value = internalState.value.copy(
            weightInput = value,
            errorMessage = null,
            weightError = validateWeight(value)
        )
    }

    fun handleNoteChange(value: String) {
        internalState.value = internalState.value.copy(note = value)
    }

    private fun validateTemperature(value: String): String? {
        if (value.isBlank()) return null
        val v = value.toDoubleOrNull() ?: return "Podaj liczbę"
        val s = internalState.value.userSettings
        if (v < s.temperatureMin || v > s.temperatureMax) return "Zakres: ${s.temperatureMin}–${s.temperatureMax} °C"
        return null
    }

    private fun validateSystolic(value: String): String? {
        if (value.isBlank()) return null
        val v = value.toIntOrNull() ?: return "Podaj liczbę"
        val s = internalState.value.userSettings
        if (v < s.systolicMin || v > s.systolicMax) return "Zakres: ${s.systolicMin}–${s.systolicMax} mmHg"
        return null
    }

    private fun validateDiastolic(value: String): String? {
        if (value.isBlank()) return null
        val v = value.toIntOrNull() ?: return "Podaj liczbę"
        val s = internalState.value.userSettings
        if (v < s.diastolicMin || v > s.diastolicMax) return "Zakres: ${s.diastolicMin}–${s.diastolicMax} mmHg"
        return null
    }

    private fun validateSugar(value: String): String? {
        if (value.isBlank()) return null
        val v = value.toDoubleOrNull() ?: return "Podaj liczbę"
        val s = internalState.value.userSettings
        if (v < s.bloodSugarMin || v > s.bloodSugarMax) return "Zakres: ${s.bloodSugarMin}–${s.bloodSugarMax} mg/dL"
        return null
    }

    private fun validateWeight(value: String): String? {
        if (value.isBlank()) return null
        val v = value.toDoubleOrNull() ?: return "Podaj liczbę"
        val s = internalState.value.userSettings
        if (v < s.weightMin || v > s.weightMax) return "Zakres: ${s.weightMin}–${s.weightMax} kg"
        return null
    }

    fun handleSave(onSuccess: () -> Unit) {
        val state = internalState.value
        val timestamp = LocalDateTime.of(state.date, state.time)
        val isEdit = state.editMeasurementId != null

        if (isEdit) {
            val measurement = buildMeasurementForEdit(state, timestamp) ?: return
            internalState.value = state.copy(isSaving = true, errorMessage = null)
            viewModelScope.launch {
                try {
                    measurementRepository.saveMeasurement(measurement)
                    measurementAlertService.handleNewMeasurement(measurement)
                    onSuccess()
                } catch (t: Throwable) {
                    internalState.value = state.copy(isSaving = false, errorMessage = "Nie udało się zapisać.")
                }
            }
            return
        }

        val toSave = buildMeasurementsForAdd(state, timestamp)
        if (toSave.isEmpty()) {
            internalState.value = state.copy(errorMessage = "Wypełnij co najmniej jeden parametr (temperatura, ciśnienie, cukier lub masa).")
            return
        }
        var hasError = false
        toSave.forEach { m ->
            when (m.type) {
                MeasurementType.TEMPERATURE -> if (validateTemperature(m.temperatureCelsius?.toString() ?: "") != null) hasError = true
                MeasurementType.BLOOD_PRESSURE -> {
                    if (validateSystolic(m.systolicPressure?.toString() ?: "") != null ||
                        validateDiastolic(m.diastolicPressure?.toString() ?: "") != null
                    ) hasError = true
                }
                MeasurementType.BLOOD_SUGAR -> if (validateSugar(m.bloodSugarMgPerDl?.toString() ?: "") != null) hasError = true
                MeasurementType.WEIGHT -> if (validateWeight(m.weightKg?.toString() ?: "") != null) hasError = true
            }
        }
        if (hasError) {
            internalState.value = state.copy(errorMessage = "Popraw wartości poza zakresem.")
            return
        }

        internalState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                toSave.forEach { measurement ->
                    measurementRepository.saveMeasurement(measurement)
                    measurementAlertService.handleNewMeasurement(measurement)
                }
                onSuccess()
            } catch (t: Throwable) {
                internalState.value = state.copy(isSaving = false, errorMessage = "Nie udało się zapisać.")
            }
        }
    }

    private fun buildMeasurementForEdit(state: AddEditMeasurementUiState, timestamp: LocalDateTime): Measurement? {
        val id = state.editMeasurementId ?: return null
        val type = state.editType ?: return null
        val note = state.note.ifBlank { null }
        return when (type) {
            MeasurementType.TEMPERATURE -> {
                val v = state.temperatureInput.toDoubleOrNull() ?: run {
                    internalState.value = state.copy(errorMessage = "Podaj temperaturę.")
                    return null
                }
                Measurement(id, timestamp, type, temperatureCelsius = v, note = note)
            }
            MeasurementType.BLOOD_PRESSURE -> {
                val sys = state.systolicInput.toIntOrNull()
                val dia = state.diastolicInput.toIntOrNull()
                if (sys == null || dia == null) {
                    internalState.value = state.copy(errorMessage = "Podaj ciśnienie skurczowe i rozkurczowe.")
                    return null
                }
                Measurement(id, timestamp, type, systolicPressure = sys, diastolicPressure = dia, note = note)
            }
            MeasurementType.BLOOD_SUGAR -> {
                val v = state.sugarInput.toDoubleOrNull() ?: run {
                    internalState.value = state.copy(errorMessage = "Podaj poziom cukru.")
                    return null
                }
                Measurement(id, timestamp, type, bloodSugarMgPerDl = v, note = note)
            }
            MeasurementType.WEIGHT -> {
                val v = state.weightInput.toDoubleOrNull() ?: run {
                    internalState.value = state.copy(errorMessage = "Podaj masę ciała.")
                    return null
                }
                Measurement(id, timestamp, type, weightKg = v, note = note)
            }
        }
    }

    private fun buildMeasurementsForAdd(state: AddEditMeasurementUiState, timestamp: LocalDateTime): List<Measurement> {
        val note = state.note.ifBlank { null }
        val list = mutableListOf<Measurement>()
        state.temperatureInput.trim().takeIf { it.isNotBlank() }?.toDoubleOrNull()?.let { v ->
            list.add(Measurement(0L, timestamp, MeasurementType.TEMPERATURE, temperatureCelsius = v, note = note))
        }
        val sys = state.systolicInput.trim().toIntOrNull()
        val dia = state.diastolicInput.trim().toIntOrNull()
        if (sys != null && dia != null) {
            list.add(Measurement(0L, timestamp, MeasurementType.BLOOD_PRESSURE, systolicPressure = sys, diastolicPressure = dia, note = note))
        }
        state.sugarInput.trim().takeIf { it.isNotBlank() }?.toDoubleOrNull()?.let { v ->
            list.add(Measurement(0L, timestamp, MeasurementType.BLOOD_SUGAR, bloodSugarMgPerDl = v, note = note))
        }
        state.weightInput.trim().takeIf { it.isNotBlank() }?.toDoubleOrNull()?.let { v ->
            list.add(Measurement(0L, timestamp, MeasurementType.WEIGHT, weightKg = v, note = note))
        }
        return list
    }

    fun handleDelete(onSuccess: () -> Unit) {
        val id = internalState.value.editMeasurementId ?: return
        viewModelScope.launch {
            val measurement = measurementRepository.getMeasurementById(id) ?: return@launch
            measurementRepository.deleteMeasurement(measurement)
            onSuccess()
        }
    }
}
