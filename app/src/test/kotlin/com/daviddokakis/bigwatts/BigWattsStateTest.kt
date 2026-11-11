package com.daviddokakis.bigwatts

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BigWattsStateTest {
    private val zones = listOf(0..149, 150..199, 200..249, 250..299, 300..349)
    private val state = BigWattsState(powerZones = zones, ftp = 250)

    @Test fun `the big number follows the smoothing setting`() {
        val s = BigWattsState(powerInstant = 310, power3s = 290, power5s = 280, power10s = 270)
        assertEquals(310, s.power(Smoothing.INSTANT))
        assertEquals(290, s.power(Smoothing.S3))
        assertEquals(280, s.power(Smoothing.S5))
        assertEquals(270, s.power(Smoothing.S10))
    }

    @Test fun `watts land in their zone`() {
        assertEquals(0, state.zoneOf(149))
        assertEquals(1, state.zoneOf(150))
        assertEquals(3, state.zoneOf(275))
    }

    @Test fun `watts above the top zone stay in the top zone`() {
        assertEquals(4, state.zoneOf(900))
    }

    @Test fun `no zones means no zone`() {
        assertNull(BigWattsState().zoneOf(200))
    }

    @Test fun `the needle sits where the watts are inside the zone`() {
        assertEquals(0.5f, state.positionInZone(175, 1), 0.001f)
    }

    @Test fun `the needle stays off the ends of the zone`() {
        assertEquals(0.04f, state.positionInZone(150, 1), 0.001f)
        assertEquals(0.96f, state.positionInZone(199, 1), 0.001f)
    }

    @Test fun `the open top zone is 30 percent of FTP wide`() {
        // 300 + 0.3 × 250 = 375: 337.5 W is halfway
        assertEquals(0.5f, state.positionInZone(337, 4), 0.01f)
    }

    @Test fun `zone time off or empty draws no growth`() {
        assertNull(state.copy(zoneSeconds = listOf(100, 200)).zoneGrowth(ZoneTime.OFF))
        assertNull(state.zoneGrowth(ZoneTime.ALL))
        assertNull(state.copy(zoneSeconds = listOf(0, 0, 0)).zoneGrowth(ZoneTime.PEDALLING))
    }

    @Test fun `all time counts coasting in zone 1`() {
        val s = state.copy(zoneSeconds = listOf(100, 200, 0), coastSeconds = 100)
        assertArrayEquals(floatArrayOf(1f, 1f, 0f), s.zoneGrowth(ZoneTime.ALL), 0.001f)
    }

    @Test fun `pedalling time leaves coasting out`() {
        val s = state.copy(zoneSeconds = listOf(100, 200, 0), coastSeconds = 100)
        assertArrayEquals(floatArrayOf(0.5f, 1f, 0f), s.zoneGrowth(ZoneTime.PEDALLING), 0.001f)
    }

    @Test fun `growth eases in over the first 5 minutes`() {
        // 90 s of 300: everything at 0.3 of its share
        val s = state.copy(zoneSeconds = listOf(30, 60))
        assertArrayEquals(floatArrayOf(0.15f, 0.3f), s.zoneGrowth(ZoneTime.PEDALLING), 0.001f)
    }
}
