package com.daviddokakis.bigwatts

import android.content.Context
import android.content.SharedPreferences

/** The rider's choices, stored on the Karoo (app-private). Written by the settings screen. */
object Settings {
    private const val FILE = "bigwatts"
    private const val KEY_LOOK = "look"
    private const val KEY_NUMBER = "number"
    private const val KEY_OLD_BACKGROUND = "background" // removed in v0.1.1: the Karoo can't colour an extension's tile
    private const val KEY_GAUGE = "gauge"
    private const val KEY_UNDER = "top"              // was "the line above the watts" in v0.1
    private const val KEY_ZONE_NAME = "zone_name"
    private const val KEY_SMOOTHING = "smoothing"
    private const val KEY_ZONE_TIME = "zone_time"
    private const val KEY_OLD_STYLE = "style"        // v0.1: PLAIN / NUMBER / TINT / BOTH / GAUGE

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun read(context: Context): Prefs {
        val p = prefs(context)
        migrate(p)
        val d = Prefs()
        return Prefs(
            look = enumOr(p.getString(KEY_LOOK, null), d.look),
            number = enumOr(p.getString(KEY_NUMBER, null), d.number),
            gauge = enumOr(p.getString(KEY_GAUGE, null), d.gauge),
            under = enumOr(p.getString(KEY_UNDER, null), d.under),
            zoneName = enumOr(p.getString(KEY_ZONE_NAME, null), d.zoneName),
            smoothing = enumOr(p.getString(KEY_SMOOTHING, null), d.smoothing),
            zoneTime = enumOr(p.getString(KEY_ZONE_TIME, null), d.zoneTime),
        )
    }

    /** Early builds' single Style becomes Number, so an update keeps the rider's look; the old
     *  Background setting is dropped (the field always shows the Karoo's own tile). */
    private fun migrate(p: SharedPreferences) {
        if (p.contains(KEY_OLD_BACKGROUND)) p.edit().remove(KEY_OLD_BACKGROUND).apply()
        val old = p.getString(KEY_OLD_STYLE, null) ?: return
        val number = when (old) {
            "PLAIN", "TINT" -> NumberStyle.NEUTRAL
            "GAUGE" -> NumberStyle.GAUGE
            else -> NumberStyle.ZONE
        }
        p.edit().putString(KEY_NUMBER, number.name).remove(KEY_OLD_STYLE).apply()
    }

    fun setLook(context: Context, v: Look) = put(context, KEY_LOOK, v)
    fun setNumber(context: Context, v: NumberStyle) = put(context, KEY_NUMBER, v)
    fun setGauge(context: Context, v: GaugeShape) = put(context, KEY_GAUGE, v)
    fun setUnder(context: Context, v: UnderLine) = put(context, KEY_UNDER, v)
    fun setZoneName(context: Context, v: ZoneName) = put(context, KEY_ZONE_NAME, v)
    fun setZoneTime(context: Context, v: ZoneTime) = put(context, KEY_ZONE_TIME, v)
    fun setSmoothing(context: Context, v: Smoothing) = put(context, KEY_SMOOTHING, v)

    private fun put(context: Context, key: String, v: Enum<*>) = prefs(context).edit().putString(key, v.name).apply()

    private inline fun <reified E : Enum<E>> enumOr(name: String?, default: E): E =
        name?.let { n -> enumValues<E>().firstOrNull { it.name == n } } ?: default
}
