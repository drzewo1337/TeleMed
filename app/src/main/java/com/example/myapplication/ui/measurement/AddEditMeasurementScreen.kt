package com.example.myapplication.ui.measurement

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                    viewModel.handleDelete { navController.popBackStack() }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = if (measurementId == null) "Dodaj pomiar" else "Edytuj pomiar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Wróć",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.date.format(dateFormatter),
                onValueChange = { },
                readOnly = true,
                label = { Text("Data", style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.time.format(timeFormatter),
                onValueChange = { },
                readOnly = true,
                label = { Text("Godzina", style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(20.dp))

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
            Spacer(modifier = Modifier.height(12.dp))

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
            Spacer(modifier = Modifier.height(8.dp))

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
            Spacer(modifier = Modifier.height(12.dp))

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
            Spacer(modifier = Modifier.height(12.dp))

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
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::handleNoteChange,
                label = { Text("Notatka (opcjonalnie)", style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                textStyle = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        uiState.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                viewModel.handleSave { navController.popBackStack() }
            },
            enabled = !uiState.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Zapisz",
                style = MaterialTheme.typography.labelLarge
            )
        }

        if (uiState.editMeasurementId != null) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = "Usuń pomiar",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
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
        label = { Text("$label [$unit]", style = MaterialTheme.typography.bodyMedium) },
        placeholder = { Text(hint, style = MaterialTheme.typography.bodyLarge) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = error != null,
        supportingText = error?.let {
            {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        enabled = enabled,
        textStyle = MaterialTheme.typography.bodyLarge
    )
}
