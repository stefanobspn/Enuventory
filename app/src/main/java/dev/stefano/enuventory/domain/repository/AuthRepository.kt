package dev.stefano.enuventory.domain.repository

import dev.stefano.enuventory.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak untuk operasi autentikasi dan data user.
 * Implementasinya akan pakai Firebase Auth + Firestore.
 */
interface AuthRepository {

    /**
     * Stream state user yang sedang login.
     * Mengembalikan null jika tidak ada user yang login.
     * UI akan reaktif mengikuti perubahan auth state.
     */
    fun getCurrentUser(): Flow<User?>

    /** Login dengan email dan password. */
    suspend fun signIn(email: String, password: String)

    /** Logout dari aplikasi. */
    suspend fun signOut()
}
