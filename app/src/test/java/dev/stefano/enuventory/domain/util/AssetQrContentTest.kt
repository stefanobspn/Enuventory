package dev.stefano.enuventory.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Menguji encode/decode konten QR asset -- termasuk input scan yang gak dikenal formatnya. */
class AssetQrContentTest {

    @Test
    fun `encode wraps the assetId in the Enuventory scheme with a slash separator`() {
        assertEquals("Enuventory::/asset/HW/YNRN8", AssetQrContent.encode("HW-YNRN8"))
    }

    @Test
    fun `decode reverses encode back to the original assetId`() {
        assertEquals("HW-YNRN8", AssetQrContent.decode(AssetQrContent.encode("HW-YNRN8")))
    }

    @Test
    fun `decode returns null for content that doesn't match the scheme`() {
        assertNull(AssetQrContent.decode("HW-YNRN8"))
        assertNull(AssetQrContent.decode("https://example.com"))
        assertNull(AssetQrContent.decode(""))
    }

    @Test
    fun `decode returns null for a malformed path missing the slash separator`() {
        assertNull(AssetQrContent.decode("Enuventory::/asset/HWYNRN8"))
    }
}
