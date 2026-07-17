package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.repository.CategoryRepository
import javax.inject.Inject

/** UseCase untuk menambahkan kategori baru. */
class AddCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(name: String) = categoryRepository.addCategory(name)
}
