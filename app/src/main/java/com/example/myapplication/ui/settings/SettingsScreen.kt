package com.example.myapplication.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.data.repository.SettingsRepository
import com.example.myapplication.domain.model.UserSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsRepository: SettingsRepository
) {
    val viewModel: SettingsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(settingsRepository) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopAppBar(
            title = { Text(text = "Ustawienia") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Switch(
            checked = settings.remindersEnabled,
            onCheckedChange = { checked ->
                viewModel.handleChange(settings.copy(remindersEnabled = checked))
            }
        )
        Text(text = "Przypomnienia o pomiarach")

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = settings.temperatureMax.toString(),
            onValueChange = { value ->
                val parsed = value.toDoubleOrNull()
                if (parsed != null) {
                    viewModel.handleChange(settings.copy(temperatureMax = parsed))
                }
            },
            label = { Text("Maks. temperatura [°C] (alert)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = settings.systolicMax.toString(),
            onValueChange = { value ->
                val parsed = value.toIntOrNull()
                if (parsed != null) {
                    viewModel.handleChange(settings.copy(systolicMax = parsed))
                }
            },
            label = { Text("Maks. ciśnienie skurczowe [mmHg]") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = settings.diastolicMax.toString(),
            onValueChange = { value ->
                val parsed = value.toIntOrNull()
                if (parsed != null) {
                    viewModel.handleChange(settings.copy(diastolicMax = parsed))
                }
            },
            label = { Text("Maks. ciśnienie rozkurczowe [mmHg]") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = settings.bloodSugarMax.toString(),
            onValueChange = { value ->
                val parsed = value.toDoubleOrNull()
                if (parsed != null) {
                    viewModel.handleChange(settings.copy(bloodSugarMax = parsed))
                }
            },
            label = { Text("Maks. poziom cukru [mg/dL]") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

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
            Text(text = "Zapisz ustawienia")
        }
    }
}
