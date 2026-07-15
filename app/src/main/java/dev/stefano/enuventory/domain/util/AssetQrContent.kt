package dev.stefano.enuventory.domain.util

/**
 * Format konten QR code asset: `"Enuventory::/asset/{prefix}/{suffix}"`, mengikuti struktur
 * ID dari [AssetIdGenerator] ("PREFIX-SUFFIX") tapi dibungkus skema URI-like supaya QR asset
 * gak gampang ketuker kalau ada barcode/QR lain yang ke-scan gak sengaja (plain ID pendek
 * terlalu gampang collision kalau dibaca dari QR sembarang).
 */
object AssetQrContent {
    private const val SCHEME_PREFIX = "Enuventory::/asset/"

    /**
     * Encode assetId (mis. "HW-YNRN8") jadi konten QR ("Enuventory::/asset/HW/YNRN8").
     * Cuma dash PERTAMA yang diganti jadi "/" -- assetId dari [AssetIdGenerator] selalu
     * "PREFIX-SUFFIX" (satu dash), tapi ini tetap round-trip aman walau ada dash tambahan
     * di suffix-nya.
     */
    fun encode(assetId: String): String = SCHEME_PREFIX + assetId.replaceFirst("-", "/")

    /**
     * Decode hasil scan balik ke assetId. Mengembalikan `null` kalau bukan skema yang
     * dikenal -- caller boleh fallback ke membandingkan raw text-nya langsung (kompat
     * dengan QR lama yang isinya cuma plain assetId).
     */
    fun decode(content: String): String? {
        if (!content.startsWith(SCHEME_PREFIX)) return null
        val path = content.removePrefix(SCHEME_PREFIX)
        if (!path.contains('/')) return null
        return path.replaceFirst("/", "-")
    }
}
