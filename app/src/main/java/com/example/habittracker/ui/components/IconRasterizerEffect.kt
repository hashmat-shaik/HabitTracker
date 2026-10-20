package com.example.habittracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.habittracker.widget.HabitWidgetUpdater
import com.example.habittracker.widget.IconBitmapCache
import kotlinx.coroutines.launch

/**
 * Invisible host that renders each distinct (icon, color) pairing among [habits] off-screen
 * once, captures it to a bitmap via [rememberGraphicsLayer], and stores it in
 * [IconBitmapCache] for the home-screen widget to read.
 *
 * Place this once in the composition (e.g. inside MainActivity's top-level content) and
 * pass it the current habit list; it does its own diffing and only renders what's missing.
 */
@Composable
fun IconRasterizerEffect(habits: List<HabitUiModel>) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pending = habits
        .distinctBy { IconBitmapCache.keyFor(it.icon, it.color) }
        .filterNot { IconBitmapCache.has(context, it.icon, it.color) }

    if (pending.isEmpty()) return

    // Zero footprint in the real layout: children below use requiredSize to force their
    // own dimensions regardless of the zero constraint from this wrapper, so they still
    // measure, layout, and draw normally (just without affecting anything on screen).
    Box(modifier = Modifier.size(0.dp)) {
        pending.forEach { habit ->
            val graphicsLayer = rememberGraphicsLayer()
            Box(
                modifier = Modifier
                    .requiredSize(64.dp)
                    .drawWithContent {
                        graphicsLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(graphicsLayer)
                    }
            ) {
                Icon(
                    imageVector = habit.icon,
                    contentDescription = null,
                    tint = habit.color,
                    modifier = Modifier.requiredSize(64.dp)
                )
            }

            LaunchedEffect(habit.id, habit.color) {
                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                IconBitmapCache.put(context, habit.icon, habit.color, bitmap)
                scope.launch { HabitWidgetUpdater.refreshAll(context) }
            }
        }
    }
}