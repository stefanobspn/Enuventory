package dev.stefano.enuventory.ui.screen.asset

import androidx.lifecycle.SavedStateHandle
import dev.stefano.enuventory.domain.model.Asset
import dev.stefano.enuventory.domain.model.AssetStatus
import dev.stefano.enuventory.domain.usecase.AddCategoryUseCase
import dev.stefano.enuventory.domain.usecase.GetAssetsUseCase
import dev.stefano.enuventory.domain.usecase.GetCategoriesUseCase
import dev.stefano.enuventory.domain.usecase.UpdateAssetUseCase
import dev.stefano.enuventory.domain.usecase.UploadAssetImageUseCase
import dev.stefano.enuventory.fake.FakeAssetRepository
import dev.stefano.enuventory.fake.FakeCategoryRepository
import dev.stefano.enuventory.fake.FakeStorageRepository
import dev.stefano.enuventory.fake.MainDispatcherRule
import dev.stefano.enuventory.ui.common.UiState
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Menguji [EditAssetViewModel]: prefill dari asset yang sudah ada, dan alur update-nya. */
class EditAssetViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var assetRepository: FakeAssetRepository
    private lateinit var storageRepository: FakeStorageRepository
    private lateinit var categoryRepository: FakeCategoryRepository

    private val existingAsset = Asset(
        id = "HW-EXIST",
        title = "Proyektor Epson",
        status = AssetStatus.Available,
        category = "Elektronik",
        description = "Proyektor buat presentasi",
        imageUrl = "https://fake-storage.test/assets/HW-EXIST.jpg"
    )

    private fun createViewModel(assetId: String = existingAsset.id) = EditAssetViewModel(
        savedStateHandle = SavedStateHandle(mapOf("assetId" to assetId)),
        getAssetsUseCase = GetAssetsUseCase(assetRepository),
        updateAssetUseCase = UpdateAssetUseCase(assetRepository),
        uploadAssetImageUseCase = UploadAssetImageUseCase(storageRepository),
        getCategoriesUseCase = GetCategoriesUseCase(categoryRepository),
        addCategoryUseCase = AddCategoryUseCase(categoryRepository)
    )

    @Before
    fun setUp() {
        assetRepository = FakeAssetRepository()
        storageRepository = FakeStorageRepository()
        categoryRepository = FakeCategoryRepository()
        assetRepository.setAssets(listOf(existingAsset))
    }

    @Test
    fun `addCategory persists via the repository and reports the trimmed name back`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            val job = launch { viewModel.categories.collect {} }
            var addedName: String? = null

            viewModel.addCategory("  Perkakas  ") { addedName = it }

            assertEquals("Perkakas", addedName)
            assertEquals(listOf("Perkakas"), categoryRepository.addCategoryCalls)
            assertEquals(1, viewModel.categories.value.size)

            job.cancel()
        }

    @Test
    fun `assetState reflects the existing asset for prefill`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            val job = launch { viewModel.assetState.collect {} }

            assertEquals(UiState.Success(existingAsset), viewModel.assetState.value)

            job.cancel()
        }

    @Test
    fun `assetState is Empty when the asset id does not exist`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(assetId = "HW-NOPE")
            val job = launch { viewModel.assetState.collect {} }

            assertEquals(UiState.Empty, viewModel.assetState.value)

            job.cancel()
        }

    @Test
    fun `editAsset updates the asset while keeping its id`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            var onSuccessCalled = false

            viewModel.editAsset(
                title = "Proyektor Epson V2",
                statusStr = "Maintenance",
                category = "Elektronik",
                description = "Sudah diservis",
                onSuccess = { onSuccessCalled = true }
            )

            assertTrue(onSuccessCalled)
            assertEquals(UiState.Success(Unit), viewModel.saveState.value)
            assertEquals(1, assetRepository.updateAssetCalls.size)
            val updated = assetRepository.updateAssetCalls.first()
            assertEquals(existingAsset.id, updated.id)
            assertEquals("Proyektor Epson V2", updated.title)
            assertEquals(AssetStatus.Maintenance, updated.status)
        }

    @Test
    fun `editAsset without a new image keeps the existing imageUrl`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            val job = launch { viewModel.assetState.collect {} }

            viewModel.editAsset(
                title = existingAsset.title,
                statusStr = "Tersedia",
                category = existingAsset.category,
                description = existingAsset.description,
                imageBytes = null,
                onSuccess = {}
            )

            assertEquals(0, storageRepository.uploadCalls.size)
            assertEquals(existingAsset.imageUrl, assetRepository.updateAssetCalls.first().imageUrl)

            job.cancel()
        }

    @Test
    fun `editAsset with a new image uploads it and replaces the imageUrl`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            val fakeImageBytes = byteArrayOf(1, 2, 3)

            viewModel.editAsset(
                title = existingAsset.title,
                statusStr = "Tersedia",
                category = existingAsset.category,
                description = existingAsset.description,
                imageBytes = fakeImageBytes,
                onSuccess = {}
            )

            assertEquals(1, storageRepository.uploadCalls.size)
            val (uploadedAssetId, uploadedBytes) = storageRepository.uploadCalls.first()
            assertEquals(existingAsset.id, uploadedAssetId)
            assertTrue(uploadedBytes.contentEquals(fakeImageBytes))
            assertEquals(
                "https://fake-storage.test/assets/${existingAsset.id}.jpg",
                assetRepository.updateAssetCalls.first().imageUrl
            )
        }

    @Test
    fun `editAsset surfaces an error state when image upload fails`() =
        runTest(mainDispatcherRule.testDispatcher) {
            storageRepository.uploadError = IllegalStateException("Storage down")
            val viewModel = createViewModel()
            var onSuccessCalled = false

            viewModel.editAsset(
                title = existingAsset.title,
                statusStr = "Tersedia",
                category = existingAsset.category,
                description = existingAsset.description,
                imageBytes = byteArrayOf(1, 2, 3),
                onSuccess = { onSuccessCalled = true }
            )

            assertTrue(onSuccessCalled.not())
            assertTrue(viewModel.saveState.value is UiState.Error)
            assertEquals(0, assetRepository.updateAssetCalls.size)
        }
}
