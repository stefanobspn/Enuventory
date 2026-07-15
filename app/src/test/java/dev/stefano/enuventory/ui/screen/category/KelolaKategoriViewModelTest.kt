package dev.stefano.enuventory.ui.screen.category

import dev.stefano.enuventory.domain.model.Asset
import dev.stefano.enuventory.domain.model.AssetStatus
import dev.stefano.enuventory.domain.model.Category
import dev.stefano.enuventory.domain.usecase.AddCategoryUseCase
import dev.stefano.enuventory.domain.usecase.DeleteCategoryUseCase
import dev.stefano.enuventory.domain.usecase.GetAssetsUseCase
import dev.stefano.enuventory.domain.usecase.GetCategoriesUseCase
import dev.stefano.enuventory.domain.usecase.UpdateAssetUseCase
import dev.stefano.enuventory.domain.usecase.UpdateCategoryUseCase
import dev.stefano.enuventory.fake.FakeAssetRepository
import dev.stefano.enuventory.fake.FakeCategoryRepository
import dev.stefano.enuventory.fake.MainDispatcherRule
import dev.stefano.enuventory.ui.common.UiState
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Menguji [KelolaKategoriViewModel]: usage count, add, rename-cascade, dan delete-blocked. */
class KelolaKategoriViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var categoryRepository: FakeCategoryRepository
    private lateinit var assetRepository: FakeAssetRepository

    private fun createViewModel() = KelolaKategoriViewModel(
        getCategoriesUseCase = GetCategoriesUseCase(categoryRepository),
        getAssetsUseCase = GetAssetsUseCase(assetRepository),
        addCategoryUseCase = AddCategoryUseCase(categoryRepository),
        updateCategoryUseCase = UpdateCategoryUseCase(categoryRepository),
        updateAssetUseCase = UpdateAssetUseCase(assetRepository),
        deleteCategoryUseCase = DeleteCategoryUseCase(categoryRepository)
    )

    private fun asset(id: String, category: String) = Asset(
        id = id,
        title = "Asset $id",
        status = AssetStatus.Available,
        category = category,
        description = ""
    )

    @Before
    fun setUp() {
        categoryRepository = FakeCategoryRepository()
        assetRepository = FakeAssetRepository()
    }

    @Test
    fun `categoriesState reflects usage count per category`() =
        runTest(mainDispatcherRule.testDispatcher) {
            categoryRepository.setCategories(
                listOf(Category(id = "1", name = "Elektro"), Category(id = "2", name = "IoT"))
            )
            assetRepository.setAssets(
                listOf(asset("A1", "Elektro"), asset("A2", "Elektro"), asset("A3", "Mekanik"))
            )
            val viewModel = createViewModel()
            val job = launch { viewModel.categoriesState.collect {} }

            val state = viewModel.categoriesState.value as UiState.Success
            val elektro = state.data.first { it.category.name == "Elektro" }
            val iot = state.data.first { it.category.name == "IoT" }
            assertEquals(2, elektro.usageCount)
            assertEquals(0, iot.usageCount)

            job.cancel()
        }

    @Test
    fun `addCategory persists the new category`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            var onSuccessCalled = false

            viewModel.addCategory("Mekanik") { onSuccessCalled = true }

            assertTrue(onSuccessCalled)
            assertEquals(listOf("Mekanik"), categoryRepository.addCategoryCalls)
        }

    @Test
    fun `renameCategory cascades the new name to every matching asset`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val elektro = Category(id = "1", name = "Elektro")
            categoryRepository.setCategories(listOf(elektro))
            assetRepository.setAssets(
                listOf(asset("A1", "Elektro"), asset("A2", "Elektro"), asset("A3", "Mekanik"))
            )
            val viewModel = createViewModel()
            val job = launch { viewModel.categoriesState.collect {} }
            var onSuccessCalled = false

            viewModel.renameCategory(elektro, "Electronics") { onSuccessCalled = true }

            assertTrue(onSuccessCalled)
            assertEquals(1, categoryRepository.updateCategoryCalls.size)
            assertEquals("Electronics", categoryRepository.updateCategoryCalls.first().name)
            val renamedAssets =
                assetRepository.updateAssetCalls.filter { it.category == "Electronics" }
            assertEquals(2, renamedAssets.size)
            assertTrue(renamedAssets.map { it.id }.containsAll(listOf("A1", "A2")))

            job.cancel()
        }

    @Test
    fun `deleteCategory is blocked and surfaces an error when the category is still in use`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val elektro = Category(id = "1", name = "Elektro")
            categoryRepository.setCategories(listOf(elektro))
            assetRepository.setAssets(listOf(asset("A1", "Elektro")))
            val viewModel = createViewModel()
            val job = launch { viewModel.categoriesState.collect {} }
            var onSuccessCalled = false

            val categoryUi = (viewModel.categoriesState.value as UiState.Success)
                .data.first { it.category.id == "1" }
            viewModel.deleteCategory(categoryUi) { onSuccessCalled = true }

            assertTrue(onSuccessCalled.not())
            assertEquals(0, categoryRepository.deleteCategoryCalls.size)
            assertNotNull(viewModel.actionError.value)

            job.cancel()
        }

    @Test
    fun `deleteCategory succeeds when the category is unused`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val iot = Category(id = "2", name = "IoT")
            categoryRepository.setCategories(listOf(iot))
            val viewModel = createViewModel()
            val job = launch { viewModel.categoriesState.collect {} }
            var onSuccessCalled = false

            val categoryUi = (viewModel.categoriesState.value as UiState.Success)
                .data.first { it.category.id == "2" }
            viewModel.deleteCategory(categoryUi) { onSuccessCalled = true }

            assertTrue(onSuccessCalled)
            assertEquals(listOf("2"), categoryRepository.deleteCategoryCalls)
            assertNull(viewModel.actionError.value)

            job.cancel()
        }
}
