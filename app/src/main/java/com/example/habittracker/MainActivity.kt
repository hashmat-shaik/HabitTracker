package com.example.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.ui.components.HabitCard
import com.example.habittracker.ui.components.HabitUiModel
import com.example.habittracker.ui.screens.AddHabitScreen
import com.example.habittracker.ui.theme.*
import com.example.habittracker.widget.HabitWidgetUpdater
import kotlinx.coroutines.launch
import java.time.LocalDate

private sealed class Screen {
    data object Dashboard : Screen()
    data object AddHabit : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitTrackerTheme {
                var screen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
                // Shared with the home-screen widget: both read/write this same object,
                // so a check-off here (or on the widget) is reflected in both places.
                val habits = HabitRepository.habits
                val scope = rememberCoroutineScope()
                val context = androidx.compose.ui.platform.LocalContext.current

                // Restores habits (and their completion history) saved from a previous
                // session — habits starts empty, so on a fresh install nothing is shown
                // until real habits are added.
                LaunchedEffect(Unit) {
                    HabitRepository.ensureLoaded(context)
                }

                fun refreshWidget() {
                    scope.launch { HabitWidgetUpdater.refreshAll(context) }
                }

                com.example.habittracker.ui.components.IconRasterizerEffect(habits = habits)

                when (screen) {
                    Screen.Dashboard -> DashboardScreen(
                        habits = habits,
                        onAddClick = { screen = Screen.AddHabit },
                        onCheckClick = { habit ->
                            // Always go through the repository so every completion is
                            // persisted, not just reflected in the on-screen state.
                            HabitRepository.toggleCompletion(habit.id)
                            refreshWidget()
                        },
                        onDayClick = { habit, date ->
                            // Backfill: tapping a past (or today's) heatmap cell toggles
                            // that specific day, same rules as the main checkmark button.
                            HabitRepository.toggleCompletion(habit.id, date)
                            refreshWidget()
                        },
                        onDeleteHabit = { habit ->
                            HabitRepository.removeHabit(habit.id)
                            refreshWidget()
                        }
                    )
                    Screen.AddHabit -> AddHabitScreen(
                        onClose = { screen = Screen.Dashboard },
                        onSave = { name, icon, color, target ->
                            HabitRepository.addHabit(
                                HabitUiModel(
                                    id = System.currentTimeMillis().toString(),
                                    name = name,
                                    icon = icon,
                                    color = color,
                                    completions = mutableStateMapOf(),
                                    targetPerDay = target
                                )
                            )
                            refreshWidget()
                            screen = Screen.Dashboard
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    habits: List<HabitUiModel>,
    onAddClick: () -> Unit,
    onCheckClick: (HabitUiModel) -> Unit,
    onDayClick: (HabitUiModel, LocalDate) -> Unit,
    onDeleteHabit: (HabitUiModel) -> Unit
) {
    var habitPendingDelete by remember { mutableStateOf<HabitUiModel?>(null) }

    Scaffold(
        topBar = {
            // Minimal top bar: just the add-habit action, no title/logo, no settings, no stats icon.
            TopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Default.Add, contentDescription = "Add Habit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(habits, key = { it.id }) { habit ->
                HabitCard(
                    habit = habit,
                    onCheckClick = { onCheckClick(habit) },
                    onLongPress = { habitPendingDelete = habit },
                    onDayClick = { date -> onDayClick(habit, date) }
                )
            }
        }
    }

    habitPendingDelete?.let { habit ->
        AlertDialog(
            onDismissRequest = { habitPendingDelete = null },
            title = { Text("Delete \"${habit.name}\"?") },
            text = { Text("This will remove the habit and all of its completion history.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteHabit(habit)
                        habitPendingDelete = null
                    }
                ) {
                    Text("Delete", color = Color(0xFFEF5350))
                }
            },
            dismissButton = {
                TextButton(onClick = { habitPendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}