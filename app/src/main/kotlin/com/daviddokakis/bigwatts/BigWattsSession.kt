package com.daviddokakis.bigwatts

import com.dubadai.karoo.common.*

import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.ActiveRidePage
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.RideProfile
import io.hammerhead.karooext.models.RideState
import io.hammerhead.karooext.models.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Live data from the Karoo. One instance, owned by [BigWattsExtension]. */
class BigWattsSession(private val karoo: KarooSystemService, private val zoneTime: ZoneTimeStore) {

    // One thread for all state changes, so updates never race.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default.limitedParallelism(1))
    private val _state = MutableStateFlow(BigWattsState())
    val state: StateFlow<BigWattsState> = _state

    /** The ride page on screen (null until the Karoo says), so only views on it draw. */
    private val _activePage = MutableStateFlow<RideProfile.Page?>(null)
    val activePage: StateFlow<RideProfile.Page?> = _activePage

    fun start() {
        val saved = zoneTime.load()
        _state.update { it.copy(zoneSeconds = saved.first, coastSeconds = saved.second) }
        countZoneTime()
        scope.launch { karoo.consumerFlow<ActiveRidePage>().collect { _activePage.value = it.page } }
        // All four power streams run, so changing the smoothing in Settings shows at once.
        collect(DataType.Type.POWER) { v -> _state.update { it.copy(powerInstant = v) } }
        collect(DataType.Type.SMOOTHED_3S_AVERAGE_POWER) { v -> _state.update { it.copy(power3s = v) } }
        collect(DataType.Type.SMOOTHED_5S_AVERAGE_POWER) { v -> _state.update { it.copy(power5s = v) } }
        collect(DataType.Type.SMOOTHED_10S_AVERAGE_POWER) { v -> _state.update { it.copy(power10s = v) } }
        collect(DataType.Type.HEART_RATE) { v -> _state.update { it.copy(heartRate = v) } }
        collect(DataType.Type.AVERAGE_POWER) { v -> _state.update { it.copy(avgPower = v) } }
        scope.launch {
            karoo.consumerFlow<UserProfile>().collect { profile ->
                _state.update {
                    it.copy(
                        powerZones = profile.powerZones.map { z -> z.min..z.max },
                        ftp = profile.ftp.takeIf { ftp -> ftp > 0 },
                    )
                }
            }
        }
    }

    /**
     * Zone time: every second while the ride records, one second for the zone of the instant
     * power (0 W as coasting; no power, nothing). Saved every 15 s so an app restart keeps the
     * ride's counts; cleared when the ride ends (the Karoo goes back to Idle).
     */
    private fun countZoneTime() {
        var riding: RideState = RideState.Idle
        scope.launch {
            karoo.consumerFlow<RideState>().collect { r ->
                riding = r
                if (r is RideState.Idle && (_state.value.zoneSeconds.isNotEmpty() || _state.value.coastSeconds > 0)) {
                    _state.update { it.copy(zoneSeconds = emptyList(), coastSeconds = 0) }
                    zoneTime.clear()
                }
            }
        }
        scope.launch {
            var tick = 0
            while (isActive) {
                delay(1_000)
                val s = _state.value
                val w = s.powerInstant
                if (riding !is RideState.Recording || w == null || s.powerZones.isEmpty()) continue
                _state.update {
                    if (w <= 0) it.copy(coastSeconds = it.coastSeconds + 1)
                    else {
                        val z = it.zoneOf(w) ?: return@update it
                        val secs = it.zoneSeconds.toMutableList()
                        while (secs.size < it.powerZones.size) secs.add(0)
                        secs[z] = secs[z] + 1
                        it.copy(zoneSeconds = secs)
                    }
                }
                if (++tick % 15 == 0) _state.value.let { zoneTime.save(it.zoneSeconds, it.coastSeconds) }
            }
        }
    }

    private fun collect(type: String, onValue: (Int?) -> Unit) {
        scope.launch { karoo.streamDataFlow(type).collect { s -> onValue(s.single()?.roundToInt()) } }
    }

    fun stop() {
        _state.value.let { if (it.zoneSeconds.isNotEmpty() || it.coastSeconds > 0) zoneTime.save(it.zoneSeconds, it.coastSeconds) }
        scope.cancel()
    }
}
