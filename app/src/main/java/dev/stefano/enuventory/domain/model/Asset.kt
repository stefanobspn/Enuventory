package dev.stefano.enuventory.domain.model

/**
 * Domain entity untuk sebuah asset/alat yang ada di inventori.
 *
 * Ini adalah representasi "murni" dari data bisnis — tidak ada
 * dependency ke Android framework, Room entity, maupun Firestore DTO.
 * Mapping dari/ke layer lain dilakukan di masing-masing layer.
 *
 * Model per-unit: satu asset = satu unit fisik (tidak ada konsep stock).
 */
data class Asset(
    val id: String,
    val title: String,
    val status: AssetStatus,
    val category: String,
    val description: String,
    val imageUrl: String? = null
)
