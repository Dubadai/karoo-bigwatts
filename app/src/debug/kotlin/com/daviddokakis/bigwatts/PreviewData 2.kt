package com.daviddokakis.bigwatts

/**
 * DEBUG ONLY: fixed example values for PreviewActivity's pictures (the showcase numbers: FTP 280,
 * 132 bpm, ride average 241 W, an hour of tempo for Zone time). Never feeds the live field: on the
 * Karoo, debug builds show only real data (or FakeWatts on the Mac).
 */
object PreviewData {
    private val ZONES = listOf(0..153, 154..209, 210..251, 252..293, 294..335, 336..419, 420..2000)

    fun apply(s: BigWattsState): BigWattsState = s.copy(
        heartRate = 132,
        avgPower = 241,
        powerZones = s.powerZones.ifEmpty { ZONES },
        ftp = s.ftp ?: 280,
        zoneSeconds = listOf(300, 800, 1300, 500, 150, 50, 20),
        coastSeconds = 480,
    )
}
