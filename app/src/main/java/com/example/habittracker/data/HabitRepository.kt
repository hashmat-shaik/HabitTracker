package com.example.habittracker.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.example.habittracker.ui.components.HabitUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Single in-memory source of truth for habits, shared between the app UI (MainActivity)
 * and the home screen widget — both run in the same process by default, so they can
 * both read/write this same object.
 *
 * Backed by [HabitStorage] (DataStore) so habits and their completion history survive
 * process death, app kills, and device reboots. Starts empty — there is no seeded/demo
 * data; the first thing a fresh install shows is an empty dashboard.
 *
 * IMPORTANT: this is the ONLY place that should mutate a habit's completions map. Every
 * mutation here ends with persist(). Anything that pokes the SnapshotStateMap directly
 * (e.g. from the UI layer) will look correct on screen but silently fail to survive
 * process death, since it never reaches disk.
 */
object HabitRepository {
    val habits = mutableStateListOf<HabitUiModel>()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val loadMutex = Mutex()
    private var loaded = false
    private lateinit var appContext: Context

    /**
     * Loads persisted habits from disk the first time it's called (from either the app or
     * the widget — whichever runs first after a process start). Safe to call repeatedly;
     * subsequent calls are a no-op once loading has completed.
     */
    suspend fun ensureLoaded(context: Context) {
        if (loaded) return
        loadMutex.withLock {
            if (loaded) return@withLock
            appContext = context.applicationContext
            val restored = HabitStorage.load(appContext)
            withContext(Dispatchers.Main.immediate) {
                habits.clear()
                habits.addAll(restored)
            }
            loaded = true
        }
    }

    fun addHabit(habit: HabitUiModel) {
        habits.add(habit)
        persist()
    }

    fun removeHabit(habitId: String) {
        habits.removeAll { it.id == habitId }
        persist()
    }

    /**
     * Increments [date]'s completion count for the given habit, capped at its daily target.
     * Calling it again once the target is met resets that day's count back to 0 (undo).
     *
     * Defaults to today, but callers (e.g. tapping a past day in the heatmap) can pass any
     * date so far — this is the single toggle path used by both the app UI and the widget,
     * so every completion always gets persisted.
     */
    fun toggleCompletion(habitId: String, date: LocalDate = LocalDate.now()) {
        val habit = habits.find { it.id == habitId } ?: return
        val map = habit.completions as? SnapshotStateMap<LocalDate, Int> ?: return
        val current = map[date] ?: 0
        map[date] = if (current >= habit.targetPerDay) 0 else current + 1
        persist()
    }

    /** Fires a background save of the current state after every mutation above. */
    private fun persist() {
        if (!::appContext.isInitialized) return
        // Snapshot completions into a plain map before hopping to a background thread —
        // SnapshotStateMap reads are meant to happen inside the Compose snapshot system.
        val snapshot = habits.map { it.copy(completions = LinkedHashMap(it.completions)) }
        scope.launch { HabitStorage.save(appContext, snapshot) }
    }
}