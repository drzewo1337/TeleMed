package com.example.myapplication.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.SettingsRepository
import com.example.myapplication.domain.model.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val internalState: MutableStateFlow<SettingsUiState> =
        MutableStateFlow(SettingsUiState())

    val uiState: StateFlow<SettingsUiState> =
        internalState.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    init {
        viewModelScope.launch {
            val loaded = settingsRepository.getUserSettings()
            internalState.value = SettingsUiState(settings = loaded)
        }
    }

    fun handleChange(settings: UserSettings) {
        internalState.value = internalState.value.copy(settings = settings, errorMessage = null)
    }

    fun handleSave(onSuccess: () -> Unit) {
        val current = internalState.value
        internalState.value = current.copy(isSaving = true, errorMessage = null)

        viewModelScope.launch {
            try {
                settingsRepository.saveUserSettings(current.settings)
                internalState.value = internalState.value.copy(isSaving = false)
                onSuccess()
            } catch (t: Throwable) {
                internalState.value = internalState.value.copy(
                    isSaving = false,
                    errorMessage = "Nie udało się zapisać ustawień."
                )
            }
        }
    }
}

