package com.example.habittracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.habittracker.ui.theme.DarkGray
import java.time.LocalDate

/**
 * A GitHub-style contribution heatmap.
 * Shows a grid of completions for the last [rows] * [columns] days.
 *
 * Cells for today and any past day are tappable (via [onDayClick]) so a user can back-fill
 * a day they forgot to mark — each tap behaves like the main checkmark button, but scoped
 * to that day. Future-dated cells are inert; there's nothing to tick yet.
 */
@Composable
fun Heatmap(
    completions: Map<LocalDate, Int>,
    baseColor: Color,
    targetCount: Int = 1,
    rows: Int = 7,
    columns: Int = 20,
    onDayClick: ((LocalDate) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    // Calculate the start date to fill the grid ending at today
    val totalDays = rows * columns
    val startDate = today.minusDays((totalDays - 1).toLong())

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        repeat(columns) { column ->
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                repeat(rows) { row ->
                    val date = startDate.plusDays((column * rows + row).toLong())
                    val count = completions[date] ?: 0

                    val color = when {
                        count == 0 -> DarkGray
                        else -> {
                            // Map completion percentage to alpha/intensity
                            val intensity = (count.toFloat() / targetCount).coerceIn(0.3f, 1f)
                            baseColor.copy(alpha = intensity)
                        }
                    }

                    val isTappable = onDayClick != null && !date.isAfter(today)

                    Box(
                        modifier = Modifier
                            .size(11.5.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(color)
                            .then(
                                if (isTappable) {
                                    Modifier.clickable { onDayClick?.invoke(date) }
                                } else {
                                    Modifier
                                }
                            )
                    )
                }
            }
        }
    }
}