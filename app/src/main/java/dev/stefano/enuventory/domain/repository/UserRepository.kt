package dev.stefano.enuventory.domain.repository

import dev.stefano.enuventory.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak untuk operasi pengelolaan akun RegularUser oleh Admin (layar Kelola User).
 * Terpisah dari [AuthRepository], yang fokus ke sesi login user yang sedang aktif.
 */
interface UserRepository {

    /** Mengambil semua akun dengan role RegularUser sebagai stream reaktif. */
    fun getRegularUsers(): Flow<List<User>>

    /**
     * Membuat akun RegularUser baru dengan login (email + password) yang beneran bisa
     * dipakai, tanpa nge-logout sesi Admin yang sedang aktif.
     */
    suspend fun createUser(name: String, email: String, password: String): User

    /** Mengubah nama akun user. */
    suspend fun updateUserName(uid: String, name: String)

    /**
     * Menonaktifkan/mengaktifkan kembali akun user. User yang dinonaktifkan akan ditolak
     * saat mencoba login (lihat AuthRepositoryImpl.signIn).
     */
    suspend fun setUserDisabled(uid: String, disabled: Boolean)
}
