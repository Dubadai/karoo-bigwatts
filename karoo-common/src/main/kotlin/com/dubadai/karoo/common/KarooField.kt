package com.dubadai.karoo.common

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.TypedValue
import android.widget.RemoteViews

/**
 * Showing a field drawn as one picture (header at the top), so its header always matches the
 * Karoo's own. The Karoo can make a row shorter than the size it reported (a map page with a route:
 * 141 → 123 px) without telling the app. A single fitXY picture is then squashed in height, header
 * included (capitals 17 px instead of 20, baseline too high). [views] splits the picture: the header
 * band at its true height, the body filling whatever height is left.
 *
 *   emitter.updateView(KarooField.views(context, bitmap, KarooField.headerBandPx(h)))
 */
object KarooField {

    /** Space below the header's baseline kept in the header band (icon and label bottoms). */
    private const val BAND_SLACK_PX = 6

    /**
     * Height of the header band for a tile [tileH] px tall: the label's caps top, the caps, and a
     * little slack; a label on two lines ([lines] = 2, KarooType.headerLines) adds its second line.
     */
    fun headerBandPx(tileH: Int, fullWidth: Boolean = false, lines: Int = 1): Int =
        KarooType.headerCapsTop(tileH, fullWidth) + KarooType.headerCapsPx(tileH) + BAND_SLACK_PX +
            (if (lines > 1) 15 else 0)

    /**
     * RemoteViews for [field]: its top [headerPx] rows at true height, the rest stretched to fill.
     * [description]: what the field says, for TalkBack.
     */
    /** How the body fills the room left: keep proportions, centred ([CENTER]) or at the bottom right ([END]), or [STRETCH]. */
    enum class Body { CENTER, END, STRETCH }

    fun views(context: Context, field: Bitmap, headerPx: Int, description: String? = null, body: Body = Body.CENTER): RemoteViews {
        val h = field.height
        val band = headerPx.coerceIn(1, h - 1)
        val headerPic = Bitmap.createBitmap(field, 0, 0, field.width, band)
        val bodyPic = Bitmap.createBitmap(field, 0, band, field.width, h - band)
        val layout = when (body) { Body.CENTER -> R.layout.kit_field_split_center; Body.END -> R.layout.kit_field_split_end; Body.STRETCH -> R.layout.kit_field_split }
        return RemoteViews(context.packageName, layout).apply {
            setImageViewBitmap(R.id.kit_field_header, headerPic)
            setImageViewBitmap(R.id.kit_field_body, bodyPic)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setViewLayoutHeight(R.id.kit_field_header, band.toFloat(), TypedValue.COMPLEX_UNIT_PX)
            }
            if (description != null) setContentDescription(R.id.kit_field_body, description)
        }
    }
}
