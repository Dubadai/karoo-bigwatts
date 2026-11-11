package com.daviddokakis.bigwatts

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView

/**
 * DEBUG BUILDS ONLY. Shows the field bitmap at a chosen size (default a full ride page, 478×721),
 * with fixed example values (PreviewData) and the current settings (any of them can be overridden), so screenshots can be
 * taken without changing a ride profile:
 * `adb shell am start -S -n com.daviddokakis.bigwatts/.PreviewActivity --es look KAROO --es number GAUGE --es gauge BAR --ei watts 315 --ei h 288`
 * Extras: look, number, gauge, under, zname (enum names), watts, w, h.
 */
class PreviewActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val saved = Settings.read(this)
        fun str(k: String) = intent.getStringExtra(k)
        val prefs = saved.copy(
            look = str("look")?.let { Look.valueOf(it) } ?: saved.look,
            number = str("number")?.let { NumberStyle.valueOf(it) } ?: saved.number,
            gauge = str("gauge")?.let { GaugeShape.valueOf(it) } ?: saved.gauge,
            under = str("under")?.let { UnderLine.valueOf(it) } ?: saved.under,
            zoneName = str("zname")?.let { ZoneName.valueOf(it) } ?: saved.zoneName,
        )
        val watts = intent.getIntExtra("watts", 280)
        val s = PreviewData.apply(BigWattsState()).copy(powerInstant = watts, power3s = watts, power5s = watts, power10s = watts)
        val w = intent.getIntExtra("w", 478)
        val h = intent.getIntExtra("h", 721)
        val night = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        // The field is transparent (the Karoo draws the tile), so put the tile's white/black behind it.
        val field = WattsRenderer(this).render(s, prefs, w, h, night)
        val bmp = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888).apply {
            android.graphics.Canvas(this).apply { drawColor(if (night) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()); drawBitmap(field, 0f, 0f, null) }
        }
        setContentView(ImageView(this).apply { setImageBitmap(bmp); scaleType = ImageView.ScaleType.FIT_START; setBackgroundColor(0xFF808080.toInt()) })
    }
}
