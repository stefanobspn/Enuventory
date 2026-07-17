package dev.stefano.enuventory.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.stefano.enuventory.data.repository.AssetRepositoryImpl
import dev.stefano.enuventory.data.repository.AuthRepositoryImpl
import dev.stefano.enuventory.data.repository.BorrowRepositoryImpl
import dev.stefano.enuventory.data.repository.CategoryRepositoryImpl
import dev.stefano.enuventory.data.repository.StorageRepositoryImpl
import dev.stefano.enuventory.data.repository.UserRepositoryImpl
import dev.stefano.enuventory.domain.repository.AssetRepository
import dev.stefano.enuventory.domain.repository.AuthRepository
import dev.stefano.enuventory.domain.repository.BorrowRepository
import dev.stefano.enuventory.domain.repository.CategoryRepository
import dev.stefano.enuventory.domain.repository.StorageRepository
import dev.stefano.enuventory.domain.repository.UserRepository
import javax.inject.Singleton

/**
 * Mengikat interface Repository ke implementasi konkretnya.
 *
 * Dengan @Binds, Hilt tahu: "saat ada yang minta AssetRepository,
 * berikan AssetRepositoryImpl".
 *
 * Ini adalah inti dari Dependency Inversion Principle:
 * UseCase bergantung pada interface (domain), bukan implementasi (data).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAssetRepository(
        impl: AssetRepositoryImpl
    ): AssetRepository

    @Binds
    @Singleton
    abstract fun bindBorrowRepository(
        impl: BorrowRepositoryImpl
    ): BorrowRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindThemeRepository(
        impl: dev.stefano.enuventory.data.repository.ThemeRepositoryImpl
    ): dev.stefano.enuventory.domain.repository.ThemeRepository

    @Binds
    @Singleton
    abstract fun bindStorageRepository(
        impl: StorageRepositoryImpl
    ): StorageRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository
}
