package com.daviddokakis.bigwatts

import android.content.Context

/** Zone time counts for the ride in progress, kept on the Karoo so an app restart mid-ride keeps them. */
class ZoneTimeStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("zone_time", Context.MODE_PRIVATE)

    /** Seconds pedalling per zone, and seconds at 0 W. */
    fun load(): Pair<List<Int>, Int> {
        val zones = prefs.getString(KEY_ZONES, null)?.split(",")?.mapNotNull { it.toIntOrNull() }.orEmpty()
        return zones to prefs.getInt(KEY_COAST, 0)
    }

    fun save(zones: List<Int>, coast: Int) =
        prefs.edit().putString(KEY_ZONES, zones.joinToString(",")).putInt(KEY_COAST, coast).apply()

    fun clear() = prefs.edit().clear().apply()

    private companion object {
        const val KEY_ZONES = "zones"
        const val KEY_COAST = "coast"
    }
}
