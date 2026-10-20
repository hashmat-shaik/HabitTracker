package com.example.habittracker.ui.components

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Maps between an [ImageVector] and a stable string id so a habit's icon can be written to
 * disk (HabitStorage) as plain text and looked back up on load. Backed by [HabitIconPalette],
 * the same list the "New Habit" icon picker uses, so every icon a user can ever choose is
 * guaranteed to round-trip.
 */
object IconRegistry {
    private val byName: Map<String, ImageVector> by lazy {
        HabitIconPalette.associateBy { it.name }
    }

    fun idFor(icon: ImageVector): String = icon.name

    fun iconFor(id: String): ImageVector = byName[id] ?: HabitIconPalette.first()
}