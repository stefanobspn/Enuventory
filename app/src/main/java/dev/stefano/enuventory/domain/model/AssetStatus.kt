package dev.stefano.enuventory.domain.model

/**
 * Status ketersediaan sebuah asset/alat di inventori.
 * Ini adalah domain enum — tidak bergantung pada layer UI manapun.
 */
enum class AssetStatus {
    Available,
    Reserved,
    Maintenance;

    companion object {
        /**
         * Parse string dari Firestore secara aman.
         * Dokumen lama menyimpan "Unavailable" — dipetakan ke [Reserved].
         */
        fun fromRaw(raw: String?): AssetStatus = when (raw) {
            "Reserved", "Unavailable" -> Reserved
            "Maintenance" -> Maintenance
            else -> Available
        }
    }
}
