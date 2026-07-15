package dev.stefano.enuventory.ui.screen.asset

import dev.stefano.enuventory.domain.model.AssetStatus
import dev.stefano.enuventory.domain.usecase.AddAssetUseCase
import dev.stefano.enuventory.domain.usecase.AddCategoryUseCase
import dev.stefano.enuventory.domain.usecase.GetAssetByIdUseCase
import dev.stefano.enuventory.domain.usecase.GetCategoriesUseCase
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

/**
 * Menguji [TambahAssetViewModel], khususnya perbaikan bug ID collision: generator ID lama
 * (timestamp % 100000) bisa diam-diam nge-overwrite asset lain karena `addAsset()` pakai
 * `.set()`. Sekarang ID di-generate acak lalu dicek dulu ke repository sebelum dipakai.
 */
class TambahAssetViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var assetRepository: FakeAssetRepository
    private lateinit var storageRepository: FakeStorageRepository
    private lateinit var categoryRepository: FakeCategoryRepository

    private fun createViewModel() = TambahAssetViewModel(
        addAssetUseCase = AddAssetUseCase(assetRepository),
        getAssetByIdUseCase = GetAssetByIdUseCase(assetRepository),
        uploadAssetImageUseCase = UploadAssetImageUseCase(storageRepository),
        getCategoriesUseCase = GetCategoriesUseCase(categoryRepository),
        addCategoryUseCase = AddCategoryUseCase(categoryRepository)
    )

    @Before
    fun setUp() {
        assetRepository = FakeAssetRepository()
        storageRepository = FakeStorageRepository()
        categoryRepository = FakeCategoryRepository()
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
    fun `addAsset succeeds and generated id follows the HW- format`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            var onSuccessCalled = false

            viewModel.addAsset(
                title = "Proyektor Epson",
                statusStr = "Tersedia",
                category = "Elektronik",
                description = "Proyektor buat presentasi",
                onSuccess = { onSuccessCalled = true }
            )

            assertTrue(onSuccessCalled)
            assertEquals(UiState.Success(Unit), viewModel.addState.value)
            assertEquals(1, assetRepository.addAssetCalls.size)
            val added = assetRepository.addAssetCalls.first()
            assertTrue(Regex("^HW-[23456789ABCDEFGHJKMNPQRSTVWXYZ]{5}$").matches(added.id))
            assertEquals(AssetStatus.Available, added.status)
        }

    @Test
    fun `addAsset retries id generation when the first candidate id is already taken`() =
        runTest(mainDispatcherRule.testDispatcher) {
            assetRepository.collideForNextNLookups =
                2 // 2 collision dulu, baru berhasil di attempt ke-3
            val viewModel = createViewModel()
            var onSuccessCalled = false

            viewModel.addAsset(
                title = "Solder",
                statusStr = "Tersedia",
                category = "Perkakas",
                description = "",
                onSuccess = { onSuccessCalled = true }
            )

            assertTrue(onSuccessCalled)
            assertEquals(1, assetRepository.addAssetCalls.size)
        }

    @Test
    fun `addAsset fails gracefully when id keeps colliding past the retry limit`() =
        runTest(mainDispatcherRule.testDispatcher) {
            assetRepository.collideForNextNLookups = 10 // lebih dari MAX_ID_GENERATION_ATTEMPTS
            val viewModel = createViewModel()
            var onSuccessCalled = false

            viewModel.addAsset(
                title = "Solder",
                statusStr = "Tersedia",
                category = "Perkakas",
                description = "",
                onSuccess = { onSuccessCalled = true }
            )

            assertTrue(onSuccessCalled.not())
            assertTrue(viewModel.addState.value is UiState.Error)
            assertEquals(0, assetRepository.addAssetCalls.size)
        }

    @Test
    fun `blank category defaults to All`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.addAsset(
                title = "Asset Aneh",
                statusStr = "Tersedia",
                category = "",
                description = "",
                onSuccess = {}
            )

            assertEquals("All", assetRepository.addAssetCalls.first().category)
        }

    @Test
    fun `addAsset without image leaves imageUrl null and skips upload`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.addAsset(
                title = "Proyektor Epson",
                statusStr = "Tersedia",
                category = "Elektronik",
                description = "",
                imageBytes = null,
                onSuccess = {}
            )

            assertEquals(0, storageRepository.uploadCalls.size)
            assertEquals(null, assetRepository.addAssetCalls.first().imageUrl)
        }

    @Test
    fun `addAsset with image uploads it and sets imageUrl from the storage result`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            val fakeImageBytes = byteArrayOf(1, 2, 3)

            viewModel.addAsset(
                title = "Proyektor Epson",
                statusStr = "Tersedia",
                category = "Elektronik",
                description = "",
                imageBytes = fakeImageBytes,
                onSuccess = {}
            )

            assertEquals(1, storageRepository.uploadCalls.size)
            val (uploadedAssetId, uploadedBytes) = storageRepository.uploadCalls.first()
            assertTrue(uploadedBytes.contentEquals(fakeImageBytes))
            val added = assetRepository.addAssetCalls.first()
            assertEquals(uploadedAssetId, added.id)
            assertEquals("https://fake-storage.test/assets/$uploadedAssetId.jpg", added.imageUrl)
        }

    @Test
    fun `addAsset surfaces an error state when image upload fails`() =
        runTest(mainDispatcherRule.testDispatcher) {
            storageRepository.uploadError = IllegalStateException("Storage down")
            val viewModel = createViewModel()
            var onSuccessCalled = false

            viewModel.addAsset(
                title = "Proyektor Epson",
                statusStr = "Tersedia",
                category = "Elektronik",
                description = "",
                imageBytes = byteArrayOf(1, 2, 3),
                onSuccess = { onSuccessCalled = true }
            )

            assertTrue(onSuccessCalled.not())
            assertTrue(viewModel.addState.value is UiState.Error)
            assertEquals(0, assetRepository.addAssetCalls.size)
        }
}
