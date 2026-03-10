package com.example.myapplication.ui.measurement

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMeasurementScreen(
    navController: NavController,
    measurementRepository: MeasurementRepository,
    settingsRepository: SettingsRepository,
    measurementId: Long?,
    initialSelectedDate: String?
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
                        measurementId = measurementId,
                        initialSelectedDate = initialSelectedDate
                    ) as T
                }
            }
        )
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = if (measurementId == null) "Dodaj pomiar" else "Edytuj pomiar",
                    fontWeight = FontWeight.Bold
                )
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Data",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            OutlinedTextField(
                value = uiState.date.format(dateFormatter),
                onValueChange = { },
                readOnly = true,
                label = { Text("Data") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Godzina",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            OutlinedTextField(
                value = uiState.time.format(timeFormatter),
                onValueChange = { },
                readOnly = true,
                label = { Text("Godzina") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            MeasurementRow(
                label = "Temperatura",
                unit = "°C",
                value = uiState.temperatureInput,
                onValueChange = viewModel::handleTemperatureChange,
                error = uiState.temperatureError,
                hint = "np. 36,6",
                isEditMode = uiState.editType == MeasurementType.TEMPERATURE,
                editOnly = uiState.editMeasurementId != null
            )
            Spacer(modifier = Modifier.height(8.dp))

            MeasurementRow(
                label = "Ciśnienie skurczowe",
                unit = "mmHg",
                value = uiState.systolicInput,
                onValueChange = viewModel::handleSystolicChange,
                error = uiState.systolicError,
                hint = "np. 120",
                isEditMode = uiState.editType == MeasurementType.BLOOD_PRESSURE,
                editOnly = uiState.editMeasurementId != null
            )
            Spacer(modifier = Modifier.height(4.dp))

            MeasurementRow(
                label = "Ciśnienie rozkurczowe",
                unit = "mmHg",
                value = uiState.diastolicInput,
                onValueChange = viewModel::handleDiastolicChange,
                error = uiState.diastolicError,
                hint = "np. 80",
                isEditMode = uiState.editType == MeasurementType.BLOOD_PRESSURE,
                editOnly = uiState.editMeasurementId != null
            )
            Spacer(modifier = Modifier.height(8.dp))

            MeasurementRow(
                label = "Poziom cukru",
                unit = "mg/dL",
                value = uiState.sugarInput,
                onValueChange = viewModel::handleSugarChange,
                error = uiState.sugarError,
                hint = "np. 100",
                isEditMode = uiState.editType == MeasurementType.BLOOD_SUGAR,
                editOnly = uiState.editMeasurementId != null
            )
            Spacer(modifier = Modifier.height(8.dp))

            MeasurementRow(
                label = "Masa ciała",
                unit = "kg",
                value = uiState.weightInput,
                onValueChange = viewModel::handleWeightChange,
                error = uiState.weightError,
                hint = "np. 70",
                isEditMode = uiState.editType == MeasurementType.WEIGHT,
                editOnly = uiState.editMeasurementId != null
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Notatka (opcjonalnie)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::handleNoteChange,
                label = { Text("Notatka") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        uiState.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                viewModel.handleSave { navController.popBackStack() }
            },
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Zapisz")
        }

        if (uiState.editMeasurementId != null) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    viewModel.handleDelete { navController.popBackStack() }
                },
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Usuń pomiar",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun MeasurementRow(
    label: String,
    unit: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    hint: String,
    isEditMode: Boolean,
    editOnly: Boolean
) {
    val enabled = !editOnly || isEditMode
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("$label [$unit]") },
        placeholder = { Text(hint) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        enabled = enabled
    )
}
