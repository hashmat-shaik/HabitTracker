package com.example.habittracker.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

/**
 * Call this after any in-app change to HabitRepository (toggle/add/remove) so that
 * placed home-screen widgets pick up the change immediately instead of waiting for
 * their own next tap or the system's periodic update.
 *
 * Usage from MainActivity, inside a coroutine scope (e.g. rememberCoroutineScope()):
 *   scope.launch { HabitWidgetUpdater.refreshAll(context) }
 */
object HabitWidgetUpdater {
    suspend fun refreshAll(context: Context) {
        HabitWidget().updateAll(context)
    }
}