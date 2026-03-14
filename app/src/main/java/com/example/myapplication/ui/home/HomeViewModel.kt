package com.example.myapplication.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.MeasurementRepository
import com.example.myapplication.domain.model.Measurement
import com.example.myapplication.domain.model.MeasurementType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val lastMeasurements: Map<MeasurementType, Measurement> = emptyMap()
)

class HomeViewModel(
    measurementRepository: MeasurementRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        measurementRepository.observeLatestMeasurements(50)
            .map { measurements ->
                val lastPerType = mutableMapOf<MeasurementType, Measurement>()
                measurements.forEach { m ->
                    if (!lastPerType.containsKey(m.type)) {
                        lastPerType[m.type] = m
                    }
                }
                HomeUiState(lastMeasurements = lastPerType)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState()
            )
}
