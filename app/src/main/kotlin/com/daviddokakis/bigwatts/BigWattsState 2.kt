package com.daviddokakis.bigwatts

import kotlin.math.min

/** Live data the field shows. Rendering only reads this (plus [Prefs]). */
data class BigWattsState(
    val powerInstant: Int? = null,
    val power3s: Int? = null,
    val power5s: Int? = null,
    val power10s: Int? = null,
    val heartRate: Int? = null,
    val avgPower: Int? = null,
    /** Power zones from the Karoo rider profile (usually 7), as min..max watts. */
    val powerZones: List<IntRange> = emptyList(),
    val ftp: Int? = null,
    /** Seconds pedalling (above 0 W) in each power zone this ride, while recording. */
    val zoneSeconds: List<Int> = emptyList(),
    /** Seconds at 0 W this ride, while recording (counted in zone 1 with Zone time "All time"). */
    val coastSeconds: Int = 0,
) {
    /**
     * How much thicker each zone's segment is drawn for Zone time, 0..1: its time relative to the
     * zone with the most time, eased in over the first 5 minutes so the start doesn't jump.
     * Null when off or before any time is counted.
     */
    fun zoneGrowth(mode: ZoneTime): FloatArray? {
        if (mode == ZoneTime.OFF || zoneSeconds.isEmpty()) return null
        val secs = zoneSeconds.mapIndexed { i, v -> if (i == 0 && mode == ZoneTime.ALL) v + coastSeconds else v }
        val top = secs.max()
        if (top <= 0) return null
        val ease = min(1f, secs.sum() / 300f)
        return FloatArray(secs.size) { secs[it].toFloat() / top * ease }
    }

    fun power(smoothing: Smoothing): Int? = when (smoothing) {
        Smoothing.INSTANT -> powerInstant
        Smoothing.S3 -> power3s
        Smoothing.S5 -> power5s
        Smoothing.S10 -> power10s
    }

    /** 0-based zone index for [watts], or null without zones. */
    fun zoneOf(watts: Int): Int? {
        if (powerZones.isEmpty()) return null
        val i = powerZones.indexOfFirst { watts <= it.last }
        return if (i < 0) powerZones.lastIndex else i
    }

    /** Where [watts] sits inside its zone, 0..1 (kept off the very ends so the needle stays inside). */
    fun positionInZone(watts: Int, zone: Int): Float {
        val z = powerZones[zone]
        val lo = z.first.toFloat()
        val hi = if (zone == powerZones.lastIndex) lo + maxOf(1f, (ftp ?: z.first) * 0.3f) else z.last + 1f
        return ((watts - lo) / (hi - lo)).coerceIn(0.04f, 0.96f)
    }
}

/** BigWatts' own look, or the Karoo's (icon top left, field name top right, Karoo font). */
enum class Look { BIGWATTS, KAROO }

/** How the number shows the zone. */
enum class NumberStyle { NEUTRAL, ZONE, GAUGE }

/** The gauge as an arc around the number, or a flat bar under it. One row always uses the bar. */
enum class GaugeShape { ARC, BAR }

/** The secondary line under the watts. */
enum class UnderLine { ZONE, HEART_RATE, AVG_WATTS }

/** Zone label: Short "Z4", Compact "Zone 4", Long "Zone 4 · Lactate Threshold". */
enum class ZoneName { SHORT, COMPACT, LONG }

/** Which Karoo power stream the big number shows. */
enum class Smoothing { INSTANT, S3, S5, S10 }

/** The rider's choices (see [Settings]). Design: docs/design/looks/. */
data class Prefs(
    val look: Look = Look.BIGWATTS,
    val number: NumberStyle = NumberStyle.ZONE,
    val gauge: GaugeShape = GaugeShape.ARC,
    val under: UnderLine = UnderLine.ZONE,
    val zoneName: ZoneName = ZoneName.SHORT,
    val smoothing: Smoothing = Smoothing.S3,
    /** Gauge segments grow with the time spent in each zone (off, all riding time including 0 W, or pedalling only). */
    val zoneTime: ZoneTime = ZoneTime.OFF,
)

enum class ZoneTime { OFF, ALL, PEDALLING }
