package dev.stefano.enuventory.domain.util

import kotlin.random.Random

/**
 * Generator kode ID asset dalam format "HW-XXXXX": prefix tetap "HW" (Hardware) + 5 karakter
 * acak dari alfabet yang aman dibaca manusia (tanpa 0/O, 1/I/L, dan U dihilangkan juga -
 * mengikuti konvensi Crockford base32 - biar gak ambigu waktu dibaca dari stiker fisik/QR).
 *
 * Ini menggantikan generator lama (`timestamp % 100000`) yang collision-nya cuma soal waktu
 * (berulang tiap 100 detik) dan bisa diam-diam nge-overwrite asset lain karena
 * `AssetRepositoryImpl.addAsset()` pakai `.set()`, bukan `.add()`.
 */
object AssetIdGenerator {
    const val PREFIX = "HW"
    private const val CODE_LENGTH = 5
    private const val ALPHABET = "23456789ABCDEFGHJKMNPQRSTVWXYZ"

    fun generate(random: Random = Random.Default): String {
        val suffix = (1..CODE_LENGTH)
            .map { ALPHABET[random.nextInt(ALPHABET.length)] }
            .joinToString(separator = "")
        return "$PREFIX-$suffix"
    }
}
