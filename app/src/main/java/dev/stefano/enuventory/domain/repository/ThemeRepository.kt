package dev.stefano.enuventory.domain.repository

import dev.stefano.enuventory.domain.model.AppThemeMode
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    fun getThemeMode(): Flow<AppThemeMode>
    suspend fun setThemeMode(mode: AppThemeMode)
}
