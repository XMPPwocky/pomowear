package com.pomowear.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TimeUtilsTest {

    @Test
    fun `formatTime returns 00 00 for zero milliseconds`() {
        assertEquals("00:00", formatTime(0L))
    }

    @Test
    fun `formatTime returns 00 01 for one second`() {
        assertEquals("00:01", formatTime(1000L))
    }

    @Test
    fun `formatTime returns 00 30 for thirty seconds`() {
        assertEquals("00:30", formatTime(30000L))
    }

    @Test
    fun `formatTime returns 01 00 for one minute`() {
        assertEquals("01:00", formatTime(60000L))
    }

    @Test
    fun `formatTime returns 01 30 for ninety seconds`() {
        assertEquals("01:30", formatTime(90000L))
    }

    @Test
    fun `formatTime returns 25 00 for twenty-five minutes`() {
        assertEquals("25:00", formatTime(1500000L))
    }

    @Test
    fun `formatTime pads single digit minutes`() {
        assertEquals("05:00", formatTime(300000L))
    }

    @Test
    fun `formatTime pads single digit seconds`() {
        assertEquals("00:05", formatTime(5000L))
    }

    @Test
    fun `formatTime handles sub-second values by truncating`() {
        assertEquals("00:00", formatTime(500L))
        assertEquals("00:00", formatTime(999L))
    }

    @Test
    fun `formatTime handles large values`() {
        assertEquals("60:00", formatTime(3600000L)) // 1 hour
    }
}
