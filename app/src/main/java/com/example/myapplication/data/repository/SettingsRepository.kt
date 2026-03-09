package com.example.myapplication.data.repository

import com.example.myapplication.data.local.UserSettingsDao
import com.example.myapplication.data.local.toDomain
import com.example.myapplication.data.local.toEntity
import com.example.myapplication.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val userSettingsDao: UserSettingsDao
) {

    fun observeUserSettings(): Flow<UserSettings> {
        return userSettingsDao.observeUserSettings().map { entity ->
            if (entity != null) {
                return@map entity.toDomain()
            }
            UserSettings()
        }
    }

    suspend fun getUserSettings(): UserSettings {
        val existing = userSettingsDao.getUserSettings()
        if (existing != null) {
            return existing.toDomain()
        }
        val defaults = UserSettings()
        userSettingsDao.upsertUserSettings(defaults.toEntity())
        return defaults
    }

    suspend fun saveUserSettings(settings: UserSettings) {
        userSettingsDao.upsertUserSettings(settings.toEntity())
    }
}

