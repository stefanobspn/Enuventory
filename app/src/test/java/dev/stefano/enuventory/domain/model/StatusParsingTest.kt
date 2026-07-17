package dev.stefano.enuventory.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Menguji parsing string status dari Firestore (`fromRaw`) — termasuk kompat mundur
 * dengan nilai lama di doc yang sudah ada ("Unavailable" era model stock) dan
 * fallback untuk nilai tak dikenal/null.
 */
class StatusParsingTest {

    @Test
    fun `AssetStatus fromRaw parses current values`() {
        assertEquals(AssetStatus.Available, AssetStatus.fromRaw("Available"))
        assertEquals(AssetStatus.Reserved, AssetStatus.fromRaw("Reserved"))
        assertEquals(AssetStatus.Maintenance, AssetStatus.fromRaw("Maintenance"))
    }

    @Test
    fun `AssetStatus fromRaw maps legacy Unavailable to Reserved`() {
        assertEquals(AssetStatus.Reserved, AssetStatus.fromRaw("Unavailable"))
    }

    @Test
    fun `AssetStatus fromRaw falls back to Available for null or unknown values`() {
        assertEquals(AssetStatus.Available, AssetStatus.fromRaw(null))
        assertEquals(AssetStatus.Available, AssetStatus.fromRaw("nonsense"))
        assertEquals(AssetStatus.Available, AssetStatus.fromRaw(""))
    }

    @Test
    fun `BorrowStatus fromRaw parses every enum name`() {
        BorrowStatus.entries.forEach { status ->
            assertEquals(status, BorrowStatus.fromRaw(status.name))
        }
    }

    @Test
    fun `BorrowStatus fromRaw falls back to Pending for null or unknown values`() {
        assertEquals(BorrowStatus.Pending, BorrowStatus.fromRaw(null))
        assertEquals(BorrowStatus.Pending, BorrowStatus.fromRaw("nonsense"))
    }

    @Test
    fun `isOverdue is true only for Borrowed records past their deadline`() {
        val record = BorrowRecord(
            id = "r1",
            assetId = "a1",
            assetTitle = "Laptop",
            borrowerId = "u1",
            borrowerName = "Budi",
            status = BorrowStatus.Borrowed,
            requestedAt = 0L,
            borrowDate = 0L,
            returnEstimate = 1_000L,
            reason = "Test"
        )
        // Pas di deadline belum terlambat, satu millis setelahnya baru terlambat.
        assertEquals(false, record.isOverdue(nowMillis = 1_000L))
        assertEquals(true, record.isOverdue(nowMillis = 1_001L))
        // Status final/non-Borrowed tidak pernah terlambat.
        assertEquals(false, record.copy(status = BorrowStatus.Completed).isOverdue(1_001L))
        assertEquals(false, record.copy(status = BorrowStatus.WaitingPickup).isOverdue(1_001L))
    }
}
