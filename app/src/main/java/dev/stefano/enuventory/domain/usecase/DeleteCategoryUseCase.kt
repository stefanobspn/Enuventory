package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.repository.CategoryRepository
import javax.inject.Inject

/** UseCase untuk menghapus kategori. Dipakai di layar Kelola Kategori (Admin). */
class DeleteCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(categoryId: String) = categoryRepository.deleteCategory(categoryId)
}
