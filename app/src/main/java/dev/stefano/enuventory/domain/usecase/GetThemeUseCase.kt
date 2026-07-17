package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.model.AppThemeMode
import dev.stefano.enuventory.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetThemeUseCase @Inject constructor(
    private val themeRepository: ThemeRepository
) {
    operator fun invoke(): Flow<AppThemeMode> = themeRepository.getThemeMode()
}
