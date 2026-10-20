package com.example.habittracker.data

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.habittracker.ui.components.HabitUiModel
import com.example.habittracker.ui.components.IconRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

private val Context.habitDataStore by preferencesDataStore(name = "habit_tracker_prefs")
private val HABITS_KEY = stringPreferencesKey("habits_json")

/**
 * Persists the habit list (including completion history) to disk via Jetpack DataStore, so
 * it survives process death, app kills, and device reboots. Habits are serialized to a JSON
 * array manually (org.json, already on-device — no extra serialization dependency needed).
 *
 * Icons can't be serialized directly (ImageVector isn't Serializable), so they're stored via
 * [IconRegistry]'s stable string id and looked back up on load.
 */
object HabitStorage {

    suspend fun save(context: Context, habits: List<HabitUiModel>) {
        val array = JSONArray()
        habits.forEach { habit ->
            val obj = JSONObject()
            obj.put("id", habit.id)
            obj.put("name", habit.name)
            obj.put("icon", IconRegistry.idFor(habit.icon))
            obj.put("color", habit.color.toArgb())
            obj.put("target", habit.targetPerDay)

            val completionsObj = JSONObject()
            habit.completions.forEach { (date, count) ->
                completionsObj.put(date.toString(), count)
            }
            obj.put("completions", completionsObj)

            array.put(obj)
        }
        context.habitDataStore.edit { prefs -> prefs[HABITS_KEY] = array.toString() }
    }

    suspend fun load(context: Context): List<HabitUiModel> {
        val json = context.habitDataStore.data.map { it[HABITS_KEY] }.first() ?: return emptyList()
        return runCatching {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)

                val completionsObj = obj.getJSONObject("completions")
                val completionsMap = mutableStateMapOf<LocalDate, Int>()
                completionsObj.keys().forEach { key ->
                    completionsMap[LocalDate.parse(key)] = completionsObj.getInt(key)
                }

                HabitUiModel(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    icon = IconRegistry.iconFor(obj.getString("icon")),
                    color = Color(obj.getInt("color")),
                    completions = completionsMap,
                    targetPerDay = obj.getInt("target")
                )
            }
        }.getOrDefault(emptyList())
    }
}