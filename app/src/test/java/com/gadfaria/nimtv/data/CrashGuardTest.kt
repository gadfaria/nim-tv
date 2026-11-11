package com.gadfaria.nimtv.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrashGuardTest {

    private fun looping(vararg times: Long) = crashLooping(times.toList(), limit = 3, windowMs = 60_000)

    @Test
    fun `fewer crashes than the limit never trip`() {
        assertFalse(looping())
        assertFalse(looping(1_000, 2_000))
    }

    @Test
    fun `three crashes inside the window trip`() {
        assertTrue(looping(1_000, 20_000, 61_000))
    }

    @Test
    fun `three crashes spread beyond the window do not trip`() {
        assertFalse(looping(1_000, 20_000, 61_001))
    }

    @Test
    fun `only the most recent crashes count`() {
        // An old crash followed by a fresh burst still trips.
        assertTrue(looping(1_000, 500_000, 510_000, 520_000))
        // A burst long ago followed by one recent crash does not.
        assertFalse(looping(1_000, 2_000, 3_000, 500_000))
    }
}
