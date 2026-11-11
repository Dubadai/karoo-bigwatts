package com.daviddokakis.bigwatts

import com.dubadai.karoo.common.*
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.extension.KarooExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import timber.log.Timber

/** Entry point the Karoo system binds to. */
class BigWattsExtension : KarooExtension(EXTENSION_ID, "1") {

    private lateinit var karoo: KarooSystemService
    private val updateScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var model: BigWattsSession

    override val types: List<DataTypeImpl> by lazy {
        listOf(BigWattsDataType(extension, model))
    }

    override fun onCreate() {
        super.onCreate()
        if (Timber.treeCount == 0) Timber.plant(Timber.DebugTree())
        karoo = KarooSystemService(this)
        model = BigWattsSession(karoo, ZoneTimeStore(this))
        karoo.connect { connected -> Timber.i("Karoo system connected: $connected") }
        model.start()
        // Not riding: tell the rider when a newer release is on GitHub (see UpdateCheck).
        UpdateCheck(this, karoo, appName = "BigWatts").start(updateScope)
    }

    override fun onDestroy() {
        model.stop()
        updateScope.cancel()
        karoo.disconnect()
        super.onDestroy()
    }

    companion object {
        const val EXTENSION_ID = "bigwatts"
    }
}
