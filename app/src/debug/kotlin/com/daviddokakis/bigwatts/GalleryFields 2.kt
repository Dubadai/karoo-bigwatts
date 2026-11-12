package com.daviddokakis.bigwatts

import android.content.Context
import android.graphics.Bitmap

/**
 * DEBUG ONLY: what the field gallery (FieldGallery.kt) draws: BigWatts in its main looks at every
 * Karoo size, with the showcase values (design-assets/tools/field-gallery/showcase.json): 280 W
 * (zone 4 at FTP 280), 132 bpm, ride average 282 W.
 */
// gallery: no-power-neighbours (this app shows power itself)
object GalleryFields {
    /** A short stretch of riding for the README GIF (seq-00…): power builds into zone 5 and eases off, heart rate follows a beat later. */
    private val SEQ = listOf(196 to 128, 214 to 129, 238 to 131, 262 to 134, 284 to 137, 306 to 141, 324 to 145, 331 to 148, 312 to 150, 288 to 149, 258 to 146, 226 to 142)

    /** Half page is BigWatts' main size: "arc", "bar" and "number" (just the watts) lead the README. */
    val variants = listOf("arc", "bar", "number", "karoo", "hr", "karoo-bar-hr", "karoo-bar-hr-nopower", "number-nopower", "karoo-bar-hr-nopower-nohr", "zt-arc") +
        SEQ.indices.map { "seq-%02d".format(it) }
    val sizes: List<String>? = null

    /** Coggan zones for FTP 280: 55, 75, 90, 105, 120, 150 %. */
    private val zones = listOf(0..154, 155..210, 211..252, 253..294, 295..336, 337..420, 421..2000)

    fun render(context: Context, variant: String, w: Int, h: Int, night: Boolean): Bitmap {
        if (variant.startsWith("seq-")) {
            val (p, hr) = SEQ[variant.removePrefix("seq-").toInt()]
            val s = BigWattsState(powerInstant = p, power3s = p, power5s = p, power10s = p, heartRate = hr, avgPower = 282, powerZones = zones, ftp = 280, zoneSeconds = emptyList(), coastSeconds = 0)
            return WattsRenderer(context).render(s, Prefs(look = Look.BIGWATTS, number = NumberStyle.GAUGE, gauge = GaugeShape.ARC, under = UnderLine.ZONE, zoneName = ZoneName.COMPACT).copy(zoneTime = ZoneTime.OFF), w, h, night)
        }
        val s = BigWattsState(
            powerInstant = 280.takeUnless { "-nopower" in variant }, power3s = 280.takeUnless { "-nopower" in variant },
            power5s = 280.takeUnless { "-nopower" in variant }, power10s = 280.takeUnless { "-nopower" in variant },
            heartRate = 132.takeUnless { variant.endsWith("-nohr") }, avgPower = 282, powerZones = zones, ftp = 280,
            zoneSeconds = when {
                // Zone time (variants zt-<look>, zt-early-<look>), 4 minutes in: mostly Z2 so far (still easing in)
                variant.startsWith("zt-early") -> listOf(40, 120, 60, 20, 0, 0, 0)
                // an hour of tempo: most in Z3, some Z4, a few short efforts
                variant.startsWith("zt-") -> listOf(300, 900, 1500, 600, 180, 60, 20)
                else -> emptyList()
            },
            coastSeconds = if (variant.startsWith("zt-early")) 20 else if (variant.startsWith("zt-")) 480 else 0,
        )
        val prefs = when (variant.removePrefix("zt-early-").removePrefix("zt-").removeSuffix("-nohr").removeSuffix("-nopower")) {
            "bar" -> Prefs(look = Look.BIGWATTS, number = NumberStyle.GAUGE, gauge = GaugeShape.BAR, under = UnderLine.ZONE, zoneName = ZoneName.COMPACT)
            "number" -> Prefs(look = Look.BIGWATTS, number = NumberStyle.ZONE, under = UnderLine.ZONE, zoneName = ZoneName.COMPACT)
            "karoo" -> Prefs(look = Look.KAROO, number = NumberStyle.ZONE, under = UnderLine.ZONE, zoneName = ZoneName.COMPACT)
            // our own setup: Karoo look, zone bar, heart rate under the watts
            "karoo-bar-hr" -> Prefs(look = Look.KAROO, number = NumberStyle.GAUGE, gauge = GaugeShape.BAR, under = UnderLine.HEART_RATE)
            "hr" -> Prefs(look = Look.BIGWATTS, number = NumberStyle.ZONE, under = UnderLine.HEART_RATE)
            else -> Prefs(look = Look.BIGWATTS, number = NumberStyle.GAUGE, gauge = GaugeShape.ARC, under = UnderLine.ZONE, zoneName = ZoneName.COMPACT)
        }
        return WattsRenderer(context).render(s, prefs.copy(zoneTime = if (variant.startsWith("zt-")) ZoneTime.ALL else ZoneTime.OFF), w, h, night)
    }
}
