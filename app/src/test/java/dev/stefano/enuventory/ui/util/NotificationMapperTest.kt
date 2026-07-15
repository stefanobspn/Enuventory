package dev.stefano.enuventory.ui.util

import dev.stefano.enuventory.domain.model.BorrowRecord
import dev.stefano.enuventory.domain.model.BorrowStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Menguji [nearDeadlineRecords] & [deadlineMessage]: deteksi batas kembali yang mendekat/lewat. */
class NotificationMapperTest {

    private val now = 1_792_195_200_000L // titik acuan tetap biar test deterministik
    private val hourMillis = 60L * 60 * 1000

    private fun record(
        id: String,
        status: BorrowStatus,
        returnEstimate: Long
    ) = BorrowRecord(
        id = id,
        assetId = "HW-1",
        assetTitle = "Laptop",
        borrowerId = "u1",
        borrowerName = "Budi",
        status = status,
        requestedAt = now - 48 * hourMillis,
        borrowDate = now - 48 * hourMillis,
        returnEstimate = returnEstimate,
        reason = "Kebutuhan proyek"
    )

    @Test
    fun `nearDeadlineRecords includes a Borrowed record within the threshold`() {
        val records = listOf(record("r1", BorrowStatus.Borrowed, now + hourMillis))

        val result = records.nearDeadlineRecords(thresholdHours = 24, nowMillis = now)

        assertEquals(1, result.size)
        assertEquals("r1", result.first().id)
    }

    @Test
    fun `nearDeadlineRecords includes an already overdue Borrowed record`() {
        val records = listOf(record("r1", BorrowStatus.Borrowed, now - hourMillis))

        val result = records.nearDeadlineRecords(thresholdHours = 24, nowMillis = now)

        assertEquals(1, result.size)
    }

    @Test
    fun `nearDeadlineRecords includes a record exactly at the threshold boundary`() {
        val records = listOf(record("r1", BorrowStatus.Borrowed, now + 24 * hourMillis))

        val result = records.nearDeadlineRecords(thresholdHours = 24, nowMillis = now)

        assertEquals(1, result.size)
    }

    @Test
    fun `nearDeadlineRecords excludes records far beyond the threshold`() {
        val records = listOf(record("r1", BorrowStatus.Borrowed, now + 10 * 24 * hourMillis))

        val result = records.nearDeadlineRecords(thresholdHours = 24, nowMillis = now)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `nearDeadlineRecords excludes non-Borrowed records even if the date is close`() {
        val soon = now + hourMillis
        val records = listOf(
            record("r1", BorrowStatus.Pending, soon),
            record("r2", BorrowStatus.WaitingPickup, soon),
            record("r3", BorrowStatus.Completed, soon),
            record("r4", BorrowStatus.Rejected, soon),
            record("r5", BorrowStatus.Damaged, soon)
        )

        val result = records.nearDeadlineRecords(thresholdHours = 24, nowMillis = now)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `deadlineMessage flags an overdue record differently from an upcoming one`() {
        val overdue = record("r1", BorrowStatus.Borrowed, now - hourMillis)
        val upcoming = record("r2", BorrowStatus.Borrowed, now + hourMillis)

        assertTrue(overdue.deadlineMessage(now).contains("lewat"))
        assertTrue(upcoming.deadlineMessage(now).contains("Batas kembali"))
    }
}
