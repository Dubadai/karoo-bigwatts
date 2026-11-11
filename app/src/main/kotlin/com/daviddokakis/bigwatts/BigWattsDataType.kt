package com.daviddokakis.bigwatts

import com.dubadai.karoo.common.*
import android.content.Context
import android.content.res.Configuration
import android.os.SystemClock
import android.widget.RemoteViews
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.roundToInt

/** The BigWatts data field: any size, drawn as one bitmap. */
class BigWattsDataType(
    extension: String,
    private val session: BigWattsSession,
) : DataTypeImpl(extension, TYPE_ID) {

    /** Numeric fallback for places that show the field as a plain number: 3 s power. */
    override fun startStream(emitter: Emitter<StreamState>) {
        val job = CoroutineScope(Dispatchers.Default).launch {
            // Only on a change: the state also moves with HR, average and zone time, and every
            // repeated empty value is logged by the Karoo as an invalid datapoint.
            session.state.map { it.power3s }.distinctUntilChanged().collect { p ->
                // Always Streaming: anything else makes the Karoo cover our view with its own
                // "UNKNOWN / No Sensor" tile. Without power the values are empty (sent once per
                // dropout) and our view draws its own No Sensor.
                val values = if (p == null) emptyMap() else mapOf(DataType.Field.SINGLE to p.toDouble())
                emitter.onNext(StreamState.Streaming(DataPoint(dataTypeId, values = values)))
            }
        }
        emitter.setCancellable { job.cancel() }
    }

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        Timber.d("View started: $config")
        emitter.onNext(UpdateGraphicConfig(showHeader = false))
        val (w, h) = config.viewSize
        val renderer = WattsRenderer(context)
        val job = CoroutineScope(Dispatchers.Default).launch {
            // The Karoo drops updates closer than ~0.9 s apart, so every redraw uses up the next
            // ~0.9 s. The watts get them: a redraw as soon as allowed when the watts (or the
            // settings) change. Heart rate, average and Zone time come along with those redraws;
            // on their own (watts steady) they redraw only after a quiet spell, so they never make
            // the next watts wait. Every few seconds a redraw anyway.
            var lastRender = 0L
            var shown: Key? = null
            var wasOnScreen = true
            while (isActive) {
                // With BigWatts on two ride pages the Karoo can show one page's picture in the
                // other's tile, so only the view on the page on screen draws (as in ClearWind).
                val onScreen = config.preview || onScreen(config)
                if (onScreen != wasOnScreen) {
                    Timber.d("View ${if (onScreen) "on screen" else "off screen, paused"}")
                    wasOnScreen = onScreen
                    if (onScreen) shown = null
                }
                if (!onScreen) { delay(POLL_MS); continue }
                val s = session.state.value
                val prefs = Settings.read(context)
                val night = isNight(context)
                val grown = s.zoneGrowth(prefs.zoneTime)?.map { (it * 40).roundToInt() }   // redraw when a segment grows visibly
                val key = Key(s.power(prefs.smoothing), s.heartRate, s.avgPower, s.powerZones, prefs, night, grown)
                val since = SystemClock.elapsedRealtime() - lastRender
                val wattsChanged = shown == null || key.watts != shown.watts || key.prefs != shown.prefs ||
                    key.night != shown.night || key.zones != shown.zones
                val otherChanged = key != shown
                if ((wattsChanged && since >= MIN_GAP_MS) || (otherChanged && since >= SIDE_GAP_MS) || since >= IDLE_REDRAW_MS) {
                    try {
                        // Header at true size, body fills the row (karoo-common KarooField): a map page
                        // with a route makes the row shorter without telling us
                        val v = KarooField.views(context, renderer.render(s, prefs, w, h, night), KarooField.headerBandPx(h))
                        emitter.updateView(v)
                        shown = key
                    } catch (e: Exception) {
                        Timber.e(e, "Render failed")
                    }
                    lastRender = SystemClock.elapsedRealtime()
                    // Page-editing previews can keep running: one picture is enough
                    if (config.preview && shown != null) break
                }
                delay(POLL_MS)
            }
        }
        emitter.setCancellable { job.cancel() }
    }

    /** Is this view's field on the ride page on screen? Matched by grid width and height; true until the Karoo says. */
    private fun onScreen(config: ViewConfig): Boolean {
        val page = session.activePage.value ?: return true
        // The Karoo stretches a page's last field into the free rows but reports its set size: the
        // shared page-rows rule (KarooType.onPage)
        return KarooType.onPage(
            page.elements.map { it.gridSize },
            page.elements.filter { it.dataTypeId == dataTypeId }.map { it.gridSize },
            config.gridSize,
        )
    }

    private fun isNight(context: Context) =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    /** What triggers a redraw when it changes. */
    private data class Key(
        val watts: Int?,
        val hr: Int?,
        val avg: Int?,
        val zones: List<IntRange>,
        val prefs: Prefs,
        val night: Boolean,
        val grown: List<Int>?,
    )

    companion object {
        const val TYPE_ID = "big-watts"
        private const val MIN_GAP_MS = 920L
        private const val SIDE_GAP_MS = 2_500L
        private const val IDLE_REDRAW_MS = 5_000L
        private const val POLL_MS = 40L
    }
}
