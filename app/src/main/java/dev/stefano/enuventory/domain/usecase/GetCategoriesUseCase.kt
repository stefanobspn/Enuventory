package dev.stefano.enuventory.domain.usecase

import dev.stefano.enuventory.domain.model.Category
import dev.stefano.enuventory.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** UseCase untuk mengambil daftar semua kategori. */
class GetCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(): Flow<List<Category>> = categoryRepository.getCategories()
}
