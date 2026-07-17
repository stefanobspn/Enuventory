package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.model.AppThemeMode
import dev.stefano.enuventory.domain.repository.ThemeRepository
import javax.inject.Inject

class SetThemeUseCase @Inject constructor(
    private val themeRepository: ThemeRepository
) {
    suspend operator fun invoke(mode: AppThemeMode) = themeRepository.setThemeMode(mode)
}
