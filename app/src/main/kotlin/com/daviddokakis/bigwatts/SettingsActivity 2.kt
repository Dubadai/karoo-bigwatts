package com.daviddokakis.bigwatts

import com.dubadai.karoo.common.*
import android.app.Activity
import android.os.Bundle
import android.widget.LinearLayout

/**
 * BigWatts settings, opened from the Karoo's app list, built from the shared settings kit
 * (SettingsKit.kt; design: karoo-starter/docs/design/settings-kit): look, number, gauge shape,
 * Zone time, the line under the watts, zone name and power smoothing. Rows that don't apply are
 * hidden (Gauge, Zone time, Zone name). The field has no tap action, so the Karoo's long-press still
 * changes the field.
 */
class SettingsActivity : Activity() {

    private lateinit var form: LinearLayout
    private lateinit var example: ExampleCard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, dp(12), dp(24))
        }
        example = ExampleCard(this)
        setContentView(
            SettingsLayout(
                this,
                back = settingsBack(getString(R.string.back)) { finish() },
                barTitle = getString(R.string.settings),
                title = settingsTitle(getString(R.string.app_short_name)),
                card = example,
                form = form,
                pageColor = getColor(R.color.settings_bg),
                textColor = getColor(R.color.settings_title),
            ),
        )
    }

/**
     * BigWatts at half a page (its main size) with these settings, shown small: 280 W, zone 4 at
     * FTP 280, 132 bpm, ride average 282 W (the showcase values every app picture uses).
     */
    private fun showExample(prefs: Prefs) {
        val s = BigWattsState(
            powerInstant = 280, power3s = 280, power5s = 280, power10s = 280,
            heartRate = 132, avgPower = 282, powerZones = EXAMPLE_ZONES, ftp = 280,
            // An hour of tempo with 8 minutes coasting (in zone 1 with "All time")
            zoneSeconds = EXAMPLE_ZONE_TIME, coastSeconds = 480,
        )
        val night = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        val full = WattsRenderer(this).render(s, prefs, HALF_W, HALF_H, night)
        val h = dp(110)
        example.show(android.graphics.Bitmap.createScaledBitmap(full, h * HALF_W / HALF_H, h, true), getString(R.string.example_description))
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        val prefs = Settings.read(this)
        showExample(prefs)
        form.removeAllViews()

        section(R.drawable.ic_settings_look, R.string.setting_look, R.string.setting_look_help,
            listOf(R.string.look_bigwatts to Look.BIGWATTS, R.string.look_karoo to Look.KAROO), prefs.look) { Settings.setLook(this, it) }

        section(R.drawable.ic_settings_target, R.string.setting_number, R.string.setting_number_help,
            listOf(R.string.number_neutral to NumberStyle.NEUTRAL, R.string.number_zone to NumberStyle.ZONE, R.string.number_gauge to NumberStyle.GAUGE),
            prefs.number) { Settings.setNumber(this, it) }

        if (prefs.number == NumberStyle.GAUGE) {
            section(R.drawable.ic_settings_speed, R.string.setting_gauge, R.string.setting_gauge_help,
                listOf(R.string.gauge_arc to GaugeShape.ARC, R.string.gauge_bar to GaugeShape.BAR), prefs.gauge) { Settings.setGauge(this, it) }
            section(R.drawable.ic_settings_clock, R.string.setting_zone_time, R.string.setting_zone_time_help,
                listOf(R.string.zone_time_off to ZoneTime.OFF, R.string.zone_time_all to ZoneTime.ALL, R.string.zone_time_pedalling to ZoneTime.PEDALLING),
                prefs.zoneTime) { Settings.setZoneTime(this, it) }
        }

        section(R.drawable.ic_settings_under, R.string.setting_under, R.string.setting_under_help,
            listOf(R.string.under_zone to UnderLine.ZONE, R.string.under_hr to UnderLine.HEART_RATE, R.string.under_avg to UnderLine.AVG_WATTS),
            prefs.under) { Settings.setUnder(this, it) }

        if (prefs.under == UnderLine.ZONE) {
            section(R.drawable.ic_settings_zone_name, R.string.setting_zone_name, R.string.setting_zone_name_help,
                listOf(R.string.zone_name_short to ZoneName.SHORT, R.string.zone_name_compact to ZoneName.COMPACT, R.string.zone_name_long to ZoneName.LONG),
                prefs.zoneName) { Settings.setZoneName(this, it) }
        }

        section(R.drawable.ic_settings_power, R.string.setting_power, R.string.setting_power_help,
            listOf(
                R.string.power_instant to Smoothing.INSTANT, R.string.power_3s to Smoothing.S3,
                R.string.power_5s to Smoothing.S5, R.string.power_10s to Smoothing.S10,
            ),
            prefs.smoothing, divider = false) { Settings.setSmoothing(this, it) }

        form.addView(settingsInfoRow(R.drawable.ic_settings_tiles, getString(R.string.settings_size_hint)))
        settingsAppRows(form, repo = "karoo-bigwatts")
    }

    /** One setting: icon heading, the Karoo's segmented control, grey help, then a divider. */
    private fun <T> section(icon: Int, title: Int, help: Int, options: List<Pair<Int, T>>, selected: T, divider: Boolean = true, onPick: (T) -> Unit) {
        form.addView(settingsHeading(icon, getString(title)))
        form.addView(settingsSegmented(options.map { (label, value) -> getString(label) to value }, selected) { onPick(it); render() })
        form.addView(settingsHelp(getString(help)))
        if (divider) form.addView(settingsDivider())
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private companion object {
        /** A half-page field on the Karoo 3 (pixels). */
        const val HALF_W = 478
        const val HALF_H = 336
        /** Coggan zones for FTP 280 (55, 75, 90, 105, 120, 150 %). */
        val EXAMPLE_ZONES = listOf(0..154, 155..210, 211..252, 253..294, 295..336, 337..420, 421..2000)
        /** Seconds pedalling per zone for the Zone time example: an hour of tempo. */
        val EXAMPLE_ZONE_TIME = listOf(300, 900, 1500, 600, 180, 60, 20)
    }
}
