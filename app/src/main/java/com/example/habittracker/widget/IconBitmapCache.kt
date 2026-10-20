package com.example.habittracker.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Caches rasterized (icon, color) combinations as bitmaps so the Glance widget — which
 * cannot render Compose ImageVectors directly — can show the app's real habit icons
 * instead of a placeholder.
 *
 * Bitmaps are produced by [com.example.habittracker.ui.components.IconRasterizerEffect]
 * while the app UI is composed, then persisted to disk (app cache dir) so they survive
 * process death — the widget can be asked to redraw after the app process is gone.
 */
object IconBitmapCache {
    private val memoryCache = ConcurrentHashMap<String, Bitmap>()

    fun keyFor(icon: ImageVector, color: Color): String {
        val safeName = icon.name.replace(Regex("[^A-Za-z0-9]"), "_")
        val colorHex = (color.toArgb().toLong() and 0xFFFFFFFFL).toString(16)
        return "icon_${safeName}_$colorHex"
    }

    fun get(context: Context, icon: ImageVector, color: Color): Bitmap? {
        val key = keyFor(icon, color)
        memoryCache[key]?.let { return it }
        val file = File(context.cacheDir, "$key.png")
        if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)?.let { decoded ->
                memoryCache[key] = decoded
                return decoded
            }
        }
        return null
    }

    fun has(context: Context, icon: ImageVector, color: Color): Boolean =
        get(context, icon, color) != null

    fun put(context: Context, icon: ImageVector, color: Color, bitmap: Bitmap) {
        val key = keyFor(icon, color)
        memoryCache[key] = bitmap
        runCatching {
            File(context.cacheDir, "$key.png").outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }
    }
}