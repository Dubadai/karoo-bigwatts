package com.daviddokakis.bigwatts

import com.dubadai.karoo.common.*
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.graphics.PathParser
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Draws the whole field into one bitmap: the watts as big as the field allows, in the chosen look
 * (BigWatts or Karoo) and number style (neutral, zone colour, gauge arc or bar), with
 * the secondary line (zone, heart rate or ride average) under the watts. Colours follow the Karoo
 * power zones. Sizes are in Karoo 3 pixels. Design: docs/design/looks/ (round 3, the mockup this
 * is ported from).
 */
class WattsRenderer(private val context: Context) {

    private val semibold: Typeface = context.resources.getFont(R.font.plex_semibold)
    private val medium: Typeface = context.resources.getFont(R.font.plex_medium)
    private val condensed: Typeface = context.resources.getFont(R.font.plex_condensed_medium)
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val heart = PathParser.createPathFromPathData(
        "M12 21s-7.5-4.6-9.6-9.2C.9 8.4 3 4.5 6.7 4.5c2.2 0 3.6 1.2 5.3 3 1.7-1.8 3.1-3 5.3-3 3.7 0 5.8 3.9 4.3 7.3C19.5 16.4 12 21 12 21z",
    )
    private val bolt = PathParser.createPathFromPathData("M14 1 4 14h7l-2 9 10-13h-7z")
    private val capsPerPx: Float = android.graphics.Rect().also {
        text.typeface = condensed; text.textSize = 100f; text.getTextBounds("H", 0, 1, it)
    }.height() / 100f

    fun render(s: BigWattsState, prefs: Prefs, width: Int, height: Int, night: Boolean): Bitmap {
        val w = max(width, 40).toFloat()
        val h = max(height, 40).toFloat()
        val p = if (night) DARK else LIGHT
        val bmp = Bitmap.createBitmap(w.toInt(), h.toInt(), Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        val watts = s.power(prefs.smoothing)
        val zone = watts?.let { s.zoneOf(it) }
        val zc = zone?.let { min(it, ZONE_COUNT - 1) }
        // No background of our own: the bitmap stays transparent so the Karoo draws its tile (rounded
        // corners, border, white or black) like for its own fields. A full-size fill covered the
        // tile's corners and border. `bg` is the tile's white/black, for contrast and the needle outline.
        val bg = p.bg

        val oneRow = h < ONE_ROW_MAX * K
        val karoo = prefs.look == Look.KAROO
        // Karoo look: the watts in the Karoo's own value font (Relative, from the Karoo) and spacing
        val numFace = if (karoo) KarooType.value else semibold
        val numSpacing = if (karoo) KarooType.VALUE_LETTER_SPACING else 0f
        val numCap = if (karoo) digitCap(numFace) else CAP_HEIGHT      // BigWatts look: sized exactly as before
        val padY = if (oneRow) 16f * K else max(18f * K, h * 0.08f)
        val padX = if (oneRow) 14f * K else max(18f * K, w * 0.08f)
        var top = padY
        var bottom = h - padY
        var header: List<RectF>? = null
        run {
            // Karoo header in every look, where and as big as the Karoo draws its own (measured on
            // our test Karoo, docs/design/header-everywhere; the same rules as ClearWind): capitals
            // 20 px in tiles under 150 px, else 23 px, starting 21 px from the top at 142 px and
            // 26 px from 168 px up; a full-width row puts them 12 px from the top. The bolt (the
            // Karoo's own shape, in the text colour like its power fields) is centred in a 25 px
            // box 12 px from the left and ends on the baseline; the text ends 9 px from the right (drawn at 7).
            val capsPx = (if (h < 150 * K) 20f else 23f) * K
            val capsTop = if (w >= 400 * K && h < 150 * K) 12f * K
            else (21f + (h / K - 142f) * 5f / 26f).coerceIn(18f, 26f).roundToInt() * K
            val baseline = capsTop + capsPx
            val boxW = capsPx * 25f / 23f
            fill.color = p.text
            KarooType.drawGlyph(c, KarooType.Glyph.BOLT, 12f * K, baseline, boxW, fill)
            val label = context.getString(FIELD_LABELS[prefs.smoothing.ordinal])
            text.typeface = condensed; text.textSize = capsPx / capsPerPx; text.color = p.text; text.textAlign = Paint.Align.RIGHT
            c.drawText(label, w - 7f * K, baseline, text)
            val lw = text.measureText(label)
            text.textAlign = Paint.Align.CENTER
            header = listOf(RectF(0f, 0f, 12f * K + boxW + 4f * K, baseline + 2f * K), RectF(w - 16f * K - lw, 0f, w, baseline + 2f * K))
            // Clear space under the header (feedback: the watts a bit further from "3S POWER")
            top = if (oneRow) baseline + 10f * K else baseline + max(22f * K, padY * 0.9f)
            if (oneRow) bottom = h - 10f * K
        }

        val label = watts?.toString() ?: "--"
        val numColor = if (prefs.number == NumberStyle.ZONE && zc != null) readable(p.zoneText[zc], bg, p.text) else p.text
        // The secondary line under the watts (left out at one row)
        val tpx = if (oneRow) 0f else max(22f * K, minOf(h * 0.11f, w * 0.13f, 60f * K)).roundToInt().toFloat()
        val lineCap = tpx * 0.72f
        if (watts == null) {
            // No power: the Karoo's empty state (KarooType) where the watts go; heart rate or the
            // ride average stays under it (no zone without power)
            val t = context.getString(R.string.no_sensor)
            val ep = KarooType.emptyPaint(t, (width / K).roundToInt()).apply { color = p.text; textSize *= K }
            val caps = KarooType.EMPTY_CAPS_PX * K
            // The line only when it has a value: no zone without power, no "♥ –" without a strap
            val line = tpx > 0 && when (prefs.under) {
                UnderLine.ZONE -> false
                UnderLine.HEART_RATE -> s.heartRate != null
                UnderLine.AVG_WATTS -> s.avgPower != null
            }
            val gap = if (line) max(14f * K, tpx * 0.8f) else 0f
            val base = top + (bottom - top - (caps + if (line) gap + lineCap else 0f)) / 2f + caps
            c.drawText(t, w / 2, base, ep)
            if (line) underLine(c, w, base + gap + lineCap, tpx, s, prefs, null, p, bg, karoo)
            return bmp
        }
        val gauge = prefs.number == NumberStyle.GAUGE
        // Zone time: segments thicker by time in zone (not in one-row fields: no room)
        val growth = if (gauge && !oneRow) s.zoneGrowth(prefs.zoneTime) else null
        if (gauge && oneRow) bottom -= (18f + 16f) * K          // bar + gap, clear of the needle

        if (gauge && !oneRow && prefs.gauge == GaugeShape.ARC) {
            val gap = max(14f * K, tpx * 0.8f)
            val out = 3f * K                                     // needle outline
            val above = 1f + needleOut(growth) * 0.85f                  // needle tip above the arc centre line, in r
            // Karoo look: the arc may rise between the icon and the field name, 8 px clear of both.
            val gTop = if (header != null) padY else top
            val gAvail = bottom - gTop
            val wMax = (w - 2 * padX) / (2 * cos(ARC_END) + SW)
            fun place(r: Float) = gTop + (gAvail - (r * above + out + gap + lineCap)) / 2f + out + r * above
            fun hits(r: Float, cy: Float): Boolean {
                if (header == null) return false
                val rr = r * above + out
                for (k in 0..60) {
                    val a = A0 + (A1 - A0) * k / 60f
                    val x = w / 2 + cos(a) * rr
                    val y = cy + sin(a) * rr
                    if (header.any { x > it.left - 8 * K && x < it.right + 8 * K && y > it.top - 8 * K && y < it.bottom + 8 * K }) return true
                }
                return false
            }
            var r = min((gAvail - gap - lineCap - out) / above, wMax)
            var cy = place(r)
            while (r > 30 && (hits(r, cy) || cy - r * above - out < (if (header != null) 0f else top))) { r -= 2f; cy = place(r) }
            arcGauge(c, w / 2, cy, r, watts, zone, s, p, bg, growth)
            text.typeface = numFace; text.color = p.text; text.letterSpacing = numSpacing
            fit(label, r * 1.22f, r * 0.7f / numCap)
            c.drawText(label, w / 2, cy, text)
            text.letterSpacing = 0f
            if (tpx > 0) underLine(c, w, cy + gap + lineCap, tpx, s, prefs, zone, p, bg, karoo)
            return bmp
        }

        val free = bottom - top
        val gap = if (tpx > 0) max(14f * K, tpx * 0.8f) else 0f
        // Bar gauge from two rows up: number, then the zone bar, then the line, as one group
        val bar = gauge && !oneRow
        val barH = if (bar) max(22f * K, min(h * 0.07f, 44f * K)).roundToInt().toFloat() else 0f
        val grow = if (growth != null) barH * GROW / 2 else 0f      // room for the thickest segment, each side
        val barGap = if (bar) max(14f * K, tpx * 0.8f) + barH * 0.3f + grow else 0f
        val barBlock = if (bar) barGap + barH + barH * 0.3f + grow else 0f
        val capMax = free - gap - lineCap - barBlock
        text.typeface = numFace; text.color = numColor; text.letterSpacing = numSpacing
        fit(label, w - 2 * padX, capMax / numCap)
        val capH = text.textSize * numCap
        val base = top + (free - (capH + barBlock + gap + lineCap)) / 2f + capH
        c.drawText(label, w / 2, base, text)
        text.letterSpacing = 0f
        var lineY = base + gap + lineCap
        if (bar) {
            val by = base + barGap + barH / 2
            barGauge(c, padX, w - padX, by, barH, watts, zone, s, p, bg, growth)
            lineY = by + barH / 2 + barH * 0.3f + grow + gap + lineCap
        }
        if (tpx > 0) underLine(c, w, lineY, tpx, s, prefs, zone, p, bg, karoo)
        if (gauge && oneRow) {
            val y = h - 10f * K - 9f * K
            barGauge(c, padX + 8 * K, w - padX - 8 * K, y, 18f * K, watts, zone, s, p, bg)
        }
        return bmp
    }

    /** Zone (Z4 / Zone 4 / Zone 4 · name), heart rate (heart + bpm) or ride average (bolt + "Avg 241 W"), in the text colour. */
    private fun underLine(c: Canvas, w: Float, baseline: Float, size: Float, s: BigWattsState, prefs: Prefs, zone: Int?, p: Palette, bg: Int, karoo: Boolean) {
        val ink = p.text
        var px = size
        text.textAlign = Paint.Align.LEFT
        // Karoo look: everything under the watts in the Karoo's value font too (Relative)
        val face = if (karoo) KarooType.value else null
        text.letterSpacing = 0f        // the heavier strokes need the font's own spacing, or letters touch
        // Relative is monospaced: its space is a full digit wide, so words get a normal gap instead.
        // And as heavy as the Karoo's header (Plex Condensed Medium): a thin outline in the text colour.
        fun wordGap() = text.textSize * 0.28f
        fun measure(t: String): Float =
            if (face == null) text.measureText(t)
            else t.split(' ').let { ws -> ws.sumOf { text.measureText(it).toDouble() }.toFloat() + (ws.size - 1) * wordGap() }
        fun draw(t: String, x0: Float, y: Float) {
            if (face == null) { c.drawText(t, x0, y, text); return }
            text.style = Paint.Style.FILL_AND_STROKE; text.strokeWidth = text.textSize * 0.028f
            var x = x0
            for (word in t.split(' ')) { c.drawText(word, x, y, text); x += text.measureText(word) + wordGap() }
            text.style = Paint.Style.FILL; text.strokeWidth = 0f
        }
        if (prefs.under == UnderLine.ZONE) {
            val n = (zone ?: -1) + 1
            val label = when {
                zone == null -> "--"
                prefs.zoneName == ZoneName.SHORT -> "Z$n"
                else -> context.getString(if (karoo) R.string.zone_compact_caps else R.string.zone_compact, n)
            }
            val name = if (prefs.zoneName == ZoneName.LONG && zone != null && s.powerZones.size == ZONE_COUNT)
                context.getString(ZONE_NAMES[zone]).let { if (karoo) it.uppercase() else it } else ""
            val labelFace = face ?: semibold
            val nameFace = face ?: medium
            var lw: Float; var nw: Float; var dotGap: Float
            while (true) {
                text.textSize = px
                text.typeface = labelFace; lw = measure(label)
                text.typeface = nameFace; nw = if (name.isEmpty()) 0f else measure(name)
                dotGap = if (name.isEmpty()) 0f else px * 0.9f
                if (px <= 14f * K || lw + dotGap + nw <= w * 0.86f) break
                px -= 1f
            }
            var x = w / 2 - (lw + dotGap + nw) / 2
            text.color = ink; text.typeface = labelFace
            draw(label, x, baseline)
            x += lw
            if (name.isNotEmpty()) {
                fill.color = p.dot
                c.drawCircle(x + dotGap / 2, baseline - px * CAP_HEIGHT / 2, max(2.5f * K, px * 0.1f), fill)
                text.typeface = nameFace
                draw(name, x + dotGap, baseline)
            }
        } else {
            val hr = prefs.under == UnderLine.HEART_RATE
            val label = if (hr) s.heartRate?.toString() ?: "--"
            else context.getString(if (karoo) R.string.avg_line_caps else R.string.avg_line, s.avgPower?.toString() ?: "--")
            text.typeface = face ?: semibold
            while (true) {
                text.textSize = px
                if (px <= 14f * K || measure(label) + px * 1.35f <= w * 0.92f) break
                px -= 1f
            }
            val iw = px * 1.05f
            val gap = px * 0.3f
            val x = w / 2 - (iw + gap + measure(label)) / 2
            // Karoo look: the Karoo's heart shape (as on its HR field) filled in its own red (zone colour climb6)
            if (hr && karoo) KarooType.redHeartBox(px).let { hb -> KarooType.drawRedHeart(c, x + (iw - hb) / 2f, baseline, hb, fill) }
            else drawIcon(c, if (hr) heart else bolt, x, baseline - px * 0.9f, iw, if (hr) p.heart else p.bolt)
            text.color = ink
            draw(label, x + iw + gap, baseline)
        }
        text.textAlign = Paint.Align.CENTER
        text.letterSpacing = 0f
    }

    /**
     * Our BetterIntervals arc, with the zones shown the Coros / Apple way: one rounded segment per
     * Karoo zone (equal lengths), each in its zone colour; the current zone thick and bright, the
     * others thinner and quieter; the needle at the watts inside the current zone.
     */
    private fun arcGauge(c: Canvas, cx: Float, cy: Float, r: Float, watts: Int?, zone: Int?, s: BigWattsState, p: Palette, bg: Int, growth: FloatArray? = null) {
        val n = if (s.powerZones.isEmpty()) ZONE_COUNT else s.powerZones.size
        val seg = (A1 - A0) / n
        val gap = 0.02f
        val sw = r * SW
        val box = RectF(cx - r, cy - r, cx + r, cy + r)
        stroke.strokeCap = Paint.Cap.ROUND
        for (i in 0 until n) {
            val on = i == zone
            val lw = (if (on) sw else sw * 0.72f) + sw * GROW * (growth?.getOrNull(i) ?: 0f)
            val capA = lw / 2 / r
            stroke.strokeWidth = lw
            stroke.color = p.zoneArc[min(i, ZONE_COUNT - 1)]
            stroke.alpha = if (on) 255 else p.dimAlpha
            val s0 = A0 + i * seg + gap / 2 + capA
            val s1 = max(s0 + 0.001f, A0 + (i + 1) * seg - gap / 2 - capA)
            c.drawArc(box, deg(s0), deg(s1 - s0), false, stroke)
        }
        stroke.alpha = 255
        if (watts == null || zone == null) return
        val a = A0 + (zone + s.positionInZone(watts, zone)) * seg
        val no = needleOut(growth)
        needle(
            c, cx + cos(a) * r * (1 - no), cy + sin(a) * r * (1 - no),
            cx + cos(a) * r * (1 + no * 0.85f), cy + sin(a) * r * (1 + no * 0.85f),
            max(5f * K, r * 0.05f), p, bg,
        )
    }

    /** The same zones laid flat: rounded bars, the current one taller, the needle at the watts. */
    private fun barGauge(c: Canvas, x0: Float, x1: Float, y: Float, hgt: Float, watts: Int?, zone: Int?, s: BigWattsState, p: Palette, bg: Int, growth: FloatArray? = null) {
        val n = if (s.powerZones.isEmpty()) ZONE_COUNT else s.powerZones.size
        val seg = (x1 - x0) / n
        for (i in 0 until n) {
            val on = i == zone
            val hh = (if (on) hgt else hgt * 0.7f) + hgt * GROW * (growth?.getOrNull(i) ?: 0f)
            fill.color = p.zoneArc[min(i, ZONE_COUNT - 1)]
            fill.alpha = if (on) 255 else p.dimAlpha
            c.drawRoundRect(x0 + i * seg + 3 * K, y - hh / 2, x0 + (i + 1) * seg - 3 * K, y + hh / 2, hh / 2, hh / 2, fill)
        }
        fill.alpha = 255
        if (watts == null || zone == null) return
        val mx = x0 + (zone + s.positionInZone(watts, zone)) * seg
        val reach = hgt * (0.8f + if (growth != null) GROW / 2 else 0f)
        needle(c, mx, y - reach, mx, y + reach, max(5f * K, hgt * 0.25f), p, bg)
    }

    /** Needle in the text colour with an outline in the background colour, round ends (as in BetterIntervals). */
    private fun needle(c: Canvas, x0: Float, y0: Float, x1: Float, y1: Float, width: Float, p: Palette, bg: Int) {
        stroke.strokeCap = Paint.Cap.ROUND
        stroke.color = bg; stroke.strokeWidth = width + 6f * K
        c.drawLine(x0, y0, x1, y1, stroke)
        stroke.color = p.text; stroke.strokeWidth = width
        c.drawLine(x0, y0, x1, y1, stroke)
    }

    private fun drawIcon(c: Canvas, icon: Path, x: Float, y: Float, size: Float, color: Int) {
        val m = Matrix().apply { setScale(size / 24f, size / 24f); postTranslate(x, y) }
        fill.color = color
        c.drawPath(Path(icon).apply { transform(m) }, fill)
    }

    /** Largest text size (up to [maxPx]) at which [label] fits in [maxW]. Digits measured as 8 so the size holds. */
    /** Digit height per px of text size for [face] (Plex 0.698; Relative differs). */
    private fun digitCap(face: Typeface): Float = digitCaps.getOrPut(face) {
        val p = Paint().apply { typeface = face; textSize = 100f }
        val b = android.graphics.Rect(); p.getTextBounds("8", 0, 1, b); b.height() / 100f
    }
    private val digitCaps = HashMap<Typeface, Float>()

    private fun fit(label: String, maxW: Float, maxPx: Float) {
        val probe = KarooType.steadyProbe(label, minDigits = 3)   // one size up to 999 W, no jump at 100
        text.textSize = max(maxPx, 8f)
        val wAtMax = text.measureText(probe)
        if (wAtMax > maxW) text.textSize = text.textSize * maxW / wAtMax
    }

    private class Palette(
        val bg: Int, val text: Int, val dot: Int, val heart: Int, val bolt: Int, val green: Int,
        val dimAlpha: Int, val zoneArc: IntArray, val zoneText: IntArray,
    )

    companion object {
        private const val ZONE_COUNT = 7
        private const val CAP_HEIGHT = 0.698f      // IBM Plex: digits are cap height
        private const val ONE_ROW_MAX = 200f        // a ride-page row is 144 px tall
        private const val ARC_END = 0.358f          // arc ends 20.5° above horizontal, as in BetterIntervals
        private const val SW = 0.16f                // current zone thickness, × r
        private const val GROW = 0.8f              // Zone time: the most-used zone gets this much thicker, × current zone
        private const val NEEDLE_OUT = 0.15f        // needle reach past the arc, × r
        private val A0 = Math.PI.toFloat() + ARC_END
        private val A1 = 2 * Math.PI.toFloat() - ARC_END
        /** Karoo 3 pixels → this screen (1 on a Karoo 3). */
        private val K = android.content.res.Resources.getSystem().displayMetrics.density / 1.875f
        /** Needle reach past the arc centre line, × r: further with Zone time, past the thickest segment. */
        private fun needleOut(growth: FloatArray?) = NEEDLE_OUT + if (growth != null) GROW * SW / 2 else 0f
        private fun deg(rad: Float) = Math.toDegrees(rad.toDouble()).toFloat()

        private val FIELD_LABELS = intArrayOf(R.string.label_power, R.string.label_power_3s, R.string.label_power_5s, R.string.label_power_10s)
        private val ZONE_NAMES = intArrayOf(
            R.string.zone_1, R.string.zone_2, R.string.zone_3, R.string.zone_4,
            R.string.zone_5, R.string.zone_6, R.string.zone_7,
        )

        /** WCAG contrast; coloured text falls back to black / white below 4.5:1, icons below 3:1. */
        private fun lum(c: Int): Double {
            fun ch(v: Int) = (v / 255.0).let { if (it <= 0.03928) it / 12.92 else ((it + 0.055) / 1.055).pow(2.4) }
            return 0.2126 * ch(Color.red(c)) + 0.7152 * ch(Color.green(c)) + 0.0722 * ch(Color.blue(c))
        }
        private fun contrast(a: Int, b: Int): Double {
            val la = lum(a); val lb = lum(b)
            return (max(la, lb) + 0.05) / (min(la, lb) + 0.05)
        }
        private fun readable(fg: Int, bg: Int, fallback: Int) = if (contrast(fg, bg) >= 4.5) fg else fallback

        private fun c(vararg hex: String) = hex.map { Color.parseColor(it) }.toIntArray()
        // Zone colours: grey, blue, green, yellow, orange, red, purple (Z1 → Z7)
        private val LIGHT = Palette(
            bg = Color.WHITE, text = Color.BLACK, dot = Color.parseColor("#888780"),
            heart = Color.parseColor("#C62F3A"), bolt = Color.parseColor("#8A6000"), green = Color.parseColor("#129A5E"), dimAlpha = 179,
            zoneArc = c("#8F8D86", "#2F7FD8", "#3FA34D", "#E8B400", "#F07A1A", "#E0383E", "#8B4FD8"),
            zoneText = c("#5F5E5A", "#0C447C", "#27500A", "#6B4B00", "#8A3B00", "#791F1F", "#4B2A85"),
        )
        private val DARK = Palette(
            bg = Color.BLACK, text = Color.WHITE, dot = Color.parseColor("#888780"),
            heart = Color.parseColor("#FF5C66"), bolt = Color.parseColor("#FAC775"), green = Color.parseColor("#1DB872"), dimAlpha = 153,
            zoneArc = c("#A6A49C", "#4C9BF0", "#5CC46A", "#FFD02E", "#FF9440", "#FF5A5F", "#A97AF2"),
            zoneText = c("#C9C7BF", "#85B7EB", "#97C459", "#F2CF5B", "#F5A86A", "#F09595", "#C3A6F2"),
        )
    }
}
