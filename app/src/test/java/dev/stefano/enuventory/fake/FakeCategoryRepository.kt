package dev.stefano.enuventory.fake

import dev.stefano.enuventory.domain.model.Category
import dev.stefano.enuventory.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory fake untuk [CategoryRepository], dipakai di unit test ViewModel. */
class FakeCategoryRepository : CategoryRepository {

    private val categoriesFlow = MutableStateFlow<List<Category>>(emptyList())
    private var nextId = 1

    val addCategoryCalls = mutableListOf<String>()
    val updateCategoryCalls = mutableListOf<Category>()
    val deleteCategoryCalls = mutableListOf<String>()

    fun setCategories(categories: List<Category>) {
        categoriesFlow.value = categories
    }

    override fun getCategories(): Flow<List<Category>> = categoriesFlow

    override suspend fun addCategory(name: String) {
        addCategoryCalls += name
        val category = Category(id = "cat-${nextId++}", name = name)
        categoriesFlow.value = categoriesFlow.value + category
    }

    override suspend fun updateCategory(category: Category) {
        updateCategoryCalls += category
        categoriesFlow.value =
            categoriesFlow.value.map { if (it.id == category.id) category else it }
    }

    override suspend fun deleteCategory(categoryId: String) {
        deleteCategoryCalls += categoryId
        categoriesFlow.value = categoriesFlow.value.filterNot { it.id == categoryId }
    }
}
