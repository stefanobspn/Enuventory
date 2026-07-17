package dev.stefano.enuventory.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

private const val SECONDARY_FIREBASE_APP_NAME = "SecondaryAuthApp"

/**
 * Bedain [FirebaseAuth] utama (sesi Admin yang lagi login) dari instance sekunder yang cuma
 * dipakai buat bikin akun RegularUser baru -- lihat provideSecondaryFirebaseAuth().
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SecondaryAuth

/**
 * Menyediakan instance Firebase SDK.
 * Semua Firebase instance adalah Singleton — satu instance per aplikasi.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * FirebaseAuth terpisah, khusus dipakai UserRepositoryImpl.createUser() buat bikin akun
     * RegularUser baru (createUserWithEmailAndPassword) tanpa nge-logout sesi Admin yang lagi
     * aktif di FirebaseAuth utama -- createUserWithEmailAndPassword otomatis sign-in sebagai
     * user barunya kalau dipanggil dari instance yang sama dengan sesi Admin.
     */
    @Provides
    @Singleton
    @SecondaryAuth
    fun provideSecondaryFirebaseAuth(@ApplicationContext context: Context): FirebaseAuth {
        val secondaryApp = FirebaseApp.getApps(context)
            .find { it.name == SECONDARY_FIREBASE_APP_NAME }
            ?: FirebaseApp.initializeApp(
                context,
                FirebaseApp.getInstance().options,
                SECONDARY_FIREBASE_APP_NAME
            )
        return FirebaseAuth.getInstance(secondaryApp)
    }
}
