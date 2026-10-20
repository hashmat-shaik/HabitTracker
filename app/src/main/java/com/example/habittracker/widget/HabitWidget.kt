package com.example.habittracker.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.ui.components.HabitUiModel
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale
import kotlin.math.roundToInt

private val HabitIdKey = ActionParameters.Key<String>("habitId")
private val DateKey = ActionParameters.Key<String>("date")

// Capsule sizing — bumped up from the first pass per feedback.
private const val CELL_DP = 28
private const val CELL_SPACING_DP = 6
private const val ICON_COLUMN_DP = 52
private const val OUTER_PADDING_DP = 16
private const val INTER_HABIT_SPACING_DP = 10
private const val HEADER_TO_BODY_SPACING_DP = 10
private const val MAX_ROWS_PER_HABIT = 3

private val BackgroundColor = Color(0xFF121212)
private val EmptyCellColor = Color(0xFF2C2C2C)

class ToggleHabitAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        HabitRepository.ensureLoaded(context)
        val habitId = parameters[HabitIdKey] ?: return
        // Falls back to today if no date was supplied, so this action still works anywhere
        // a caller doesn't pass one.
        val date = parameters[DateKey]?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: LocalDate.now()
        HabitRepository.toggleCompletion(habitId, date)
        HabitWidget().update(context, glanceId)
    }
}

class HabitWidget : GlanceAppWidget() {

    // The widget can be resized by the user, so we recompute the grid every time it's
    // rendered rather than baking in a fixed number of day-columns or week-rows.
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        HabitRepository.ensureLoaded(context)
        provideContent {
            HabitWidgetContent()
        }
    }
}

@Composable
private fun HabitWidgetContent() {
    val habits = HabitRepository.habits
    val size = LocalSize.current
    val context = LocalContext.current

    // LocalSize gives Dp values directly (not px), so these divisions are dp/dp = count.
    val columns = ((size.width.value.roundToInt() - ICON_COLUMN_DP) / (CELL_DP + CELL_SPACING_DP))
        .coerceIn(4, 21)

    val habitsCount = habits.size.coerceAtLeast(1)
    val headerBlockDp = CELL_DP + HEADER_TO_BODY_SPACING_DP
    val perRowDp = CELL_DP + CELL_SPACING_DP
    val usableHeightDp = size.height.value.roundToInt() -
            (OUTER_PADDING_DP * 2) - headerBlockDp - (habitsCount - 1) * INTER_HABIT_SPACING_DP
    val perHabitHeightDp = (usableHeightDp / habitsCount).coerceAtLeast(perRowDp)
    // How many week-rows fit per habit, given the leftover vertical space — this is what
    // fills in the previously-empty space on taller (e.g. 2x5) widget placements.
    val rowsPerHabit = (perHabitHeightDp / perRowDp).coerceIn(1, MAX_ROWS_PER_HABIT)

    val today = LocalDate.now()
    val headerDates = datesForRow(today, columns, row = 0)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(BackgroundColor))
            .cornerRadius(24.dp)
            .padding(OUTER_PADDING_DP.dp)
    ) {
        DayHeaderRow(headerDates)
        Spacer(modifier = GlanceModifier.height(HEADER_TO_BODY_SPACING_DP.dp))

        if (habits.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxWidth().padding(top = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No habits yet",
                    style = TextStyle(color = ColorProvider(Color.Gray))
                )
            }
        } else {
            habits.forEachIndexed { index, habit ->
                HabitBlock(
                    habit = habit,
                    context = context,
                    columns = columns,
                    rowsPerHabit = rowsPerHabit,
                    today = today
                )
                if (index != habits.lastIndex) {
                    Spacer(modifier = GlanceModifier.height(INTER_HABIT_SPACING_DP.dp))
                }
            }
        }
    }
}

/** Dates for a given week-row: row 0 is the most recent [columns] days ending today; each
 *  increasing row steps back by another [columns]-day block. */
private fun datesForRow(today: LocalDate, columns: Int, row: Int): List<LocalDate> {
    val rowEndDate = today.minusDays((row * columns).toLong())
    val rowStartDate = rowEndDate.minusDays((columns - 1).toLong())
    return (0 until columns).map { rowStartDate.plusDays(it.toLong()) }
}

@Composable
private fun DayHeaderRow(dates: List<LocalDate>) {
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        Spacer(modifier = GlanceModifier.width(ICON_COLUMN_DP.dp))
        dates.forEach { date ->
            Box(
                modifier = GlanceModifier
                    .width(CELL_DP.dp)
                    .padding(end = CELL_SPACING_DP.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.dayOfWeek
                        .getDisplayName(JavaTextStyle.SHORT, Locale.getDefault())
                        .take(2),
                    style = TextStyle(
                        color = ColorProvider(Color.Gray),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun HabitBlock(
    habit: HabitUiModel,
    context: Context,
    columns: Int,
    rowsPerHabit: Int,
    today: LocalDate
) {
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        for (row in 0 until rowsPerHabit) {
            val dates = datesForRow(today, columns, row)
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (row == 0) {
                    HabitIconBadge(habit = habit, context = context)
                } else {
                    Spacer(modifier = GlanceModifier.width(ICON_COLUMN_DP.dp))
                }

                dates.forEach { date ->
                    DayCell(habit = habit, date = date, today = today)
                }
            }
            if (row != rowsPerHabit - 1) {
                Spacer(modifier = GlanceModifier.height(CELL_SPACING_DP.dp))
            }
        }
    }
}

@Composable
private fun HabitIconBadge(habit: HabitUiModel, context: Context) {
    val bitmap = IconBitmapCache.get(context, habit.icon, habit.color)

    Box(
        modifier = GlanceModifier
            .width(ICON_COLUMN_DP.dp)
            .height(CELL_DP.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = GlanceModifier
                .size(CELL_DP.dp)
                .background(ColorProvider(habit.color.copy(alpha = 0.2f)))
                .cornerRadius(10.dp),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    provider = ImageProvider(bitmap),
                    contentDescription = habit.name,
                    modifier = GlanceModifier.size((CELL_DP * 0.6).dp)
                )
            } else {
                // Rendered before the app has had a chance to rasterize this habit's real
                // icon (e.g. right after a fresh install/reboot) — falls back to a letter
                // badge and self-corrects once IconRasterizerEffect runs in the app.
                Text(
                    text = habit.name.take(1).uppercase(),
                    style = TextStyle(
                        color = ColorProvider(habit.color),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun DayCell(habit: HabitUiModel, date: LocalDate, today: LocalDate) {
    val count = habit.completions[date] ?: 0
    val cellColor = if (count <= 0) {
        EmptyCellColor
    } else {
        val intensity = (count.toFloat() / habit.targetPerDay).coerceIn(0.3f, 1f)
        habit.color.copy(alpha = intensity)
    }

    // Today and every past day can be tapped to backfill a missed completion; future days
    // (visible on wider widget sizes) are inert since there's nothing to tick yet.
    val isTappable = !date.isAfter(today)

    Box(
        modifier = GlanceModifier
            .width(CELL_DP.dp)
            .padding(end = CELL_SPACING_DP.dp)
    ) {
        val cellModifier = GlanceModifier
            .size((CELL_DP - CELL_SPACING_DP).dp)
            .background(ColorProvider(cellColor))
            .cornerRadius(6.dp)

        Box(
            modifier = if (isTappable) {
                cellModifier.clickable(
                    actionRunCallback<ToggleHabitAction>(
                        actionParametersOf(
                            HabitIdKey to habit.id,
                            DateKey to date.toString()
                        )
                    )
                )
            } else {
                cellModifier
            }
        ) {}
    }
}