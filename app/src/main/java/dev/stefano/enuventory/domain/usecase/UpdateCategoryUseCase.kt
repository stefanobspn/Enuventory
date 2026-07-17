package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.model.Category
import dev.stefano.enuventory.domain.repository.CategoryRepository
import javax.inject.Inject

/** UseCase untuk memperbarui (rename) kategori yang sudah ada. */
class UpdateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(category: Category) = categoryRepository.updateCategory(category)
}
