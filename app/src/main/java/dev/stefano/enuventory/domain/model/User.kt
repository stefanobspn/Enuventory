package dev.stefano.enuventory.domain.model

/**
 * Domain entity untuk user yang login ke aplikasi.
 */
data class User(
    val uid: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val disabled: Boolean = false
)

enum class UserRole {
    Admin, RegularUser
}
