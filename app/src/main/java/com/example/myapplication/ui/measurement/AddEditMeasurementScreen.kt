package com.example.myapplication.ui.measurement

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.data.repository.MeasurementRepository
import com.example.myapplication.data.repository.SettingsRepository
import com.example.myapplication.domain.anomaly.MeasurementAlertService
import com.example.myapplication.domain.model.MeasurementType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMeasurementScreen(
    navController: NavController,
    measurementRepository: MeasurementRepository,
    settingsRepository: SettingsRepository,
    measurementId: Long?
) {
    val context = LocalContext.current
    val alertService = remember(context, settingsRepository) {
        MeasurementAlertService(
            appContext = context.applicationContext,
            settingsRepository = settingsRepository
        )
    }
    val viewModel: AddEditMeasurementViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return AddEditMeasurementViewModel(
                        measurementRepository = measurementRepository,
                        settingsRepository = settingsRepository,
                        measurementAlertService = alertService,
                        measurementId = measurementId
                    ) as T
                }
            }
        )
    val uiState = viewModel.uiState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = if (measurementId == null) "Dodaj pomiar" else "Edytuj pomiar"
                )
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Rodzaj pomiaru",
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            MeasurementType.values().forEach { type ->
                val selected = uiState.selectedType == type
                OutlinedButton(
                    onClick = { viewModel.handleTypeChange(type) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = when (type) {
                            MeasurementType.TEMPERATURE -> "Temperatura"
                            MeasurementType.BLOOD_PRESSURE -> "Ciśnienie"
                            MeasurementType.BLOOD_SUGAR -> "Cukier"
                            MeasurementType.WEIGHT -> "Masa"
                        },
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (uiState.selectedType) {
            MeasurementType.TEMPERATURE -> {
                OutlinedTextField(
                    value = uiState.temperatureInput,
                    onValueChange = viewModel::handleTemperatureChange,
                    label = { Text("Temperatura [°C]") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            MeasurementType.BLOOD_PRESSURE -> {
                OutlinedTextField(
                    value = uiState.systolicInput,
                    onValueChange = viewModel::handleSystolicChange,
                    label = { Text("Skurczowe [mmHg]") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.diastolicInput,
                    onValueChange = viewModel::handleDiastolicChange,
                    label = { Text("Rozkurczowe [mmHg]") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            MeasurementType.BLOOD_SUGAR -> {
                OutlinedTextField(
                    value = uiState.sugarInput,
                    onValueChange = viewModel::handleSugarChange,
                    label = { Text("Poziom cukru [mg/dL]") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            MeasurementType.WEIGHT -> {
                OutlinedTextField(
                    value = uiState.weightInput,
                    onValueChange = viewModel::handleWeightChange,
                    label = { Text("Masa ciała [kg]") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.note,
            onValueChange = viewModel::handleNoteChange,
            label = { Text("Notatka (opcjonalnie)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        uiState.errorMessage?.let { error ->
            Text(text = error, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                viewModel.handleSave {
                    navController.popBackStack()
                }
            },
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Zapisz")
        }
    }
}

