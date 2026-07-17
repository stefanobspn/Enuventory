package dev.stefano.enuventory.domain.model

/**
 * Domain entity untuk kategori asset. Terpisah dari [Asset.category] (yang cuma nyimpen
 * nama kategori sebagai string) supaya kategori bisa dikelola sendiri: ditambah, di-rename,
 * dan dihapus lewat layar Kelola Kategori.
 */
data class Category(
    val id: String,
    val name: String
)
