package com.example.habittracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habittracker.ui.components.HabitIconPalette
import com.example.habittracker.ui.theme.DarkBackground
import com.example.habittracker.ui.theme.DarkGray
import com.example.habittracker.ui.theme.DarkSurface
import com.example.habittracker.ui.theme.HabitColorPalette

private enum class TrackingMode { STEP_BY_STEP, CUSTOM_VALUE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    onClose: () -> Unit,
    onSave: (name: String, icon: ImageVector, color: Color, targetPerDay: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedIconIndex by remember { mutableStateOf(0) }
    var selectedColorIndex by remember { mutableStateOf(0) }
    var trackingMode by remember { mutableStateOf(TrackingMode.STEP_BY_STEP) }
    var targetPerDay by remember { mutableStateOf(1) }

    val selectedIcon = HabitIconPalette[selectedIconIndex]
    val selectedColor = HabitColorPalette[selectedColorIndex]

    Scaffold(containerColor = DarkBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "New Habit",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live preview of the selected icon + color
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .background(selectedColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = selectedIcon,
                    contentDescription = null,
                    tint = selectedColor,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SectionLabel("Icon")
            IconGrid(
                icons = HabitIconPalette,
                selectedIndex = selectedIconIndex,
                accentColor = selectedColor,
                onSelect = { selectedIconIndex = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            SectionLabel("Name")
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Exercise") },
                singleLine = true,
                colors = darkFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            SectionLabel("Description")
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Optional") },
                colors = darkFieldColors()
            )

            Spacer(modifier = Modifier.height(20.dp))

            SectionLabel("Color")
            ColorGrid(
                colors = HabitColorPalette,
                selectedIndex = selectedColorIndex,
                onSelect = { selectedColorIndex = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionLabel("How should completions be tracked?")
            TrackingModeToggle(
                mode = trackingMode,
                onModeChange = { trackingMode = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SectionLabel("Completions Per Day")
            when (trackingMode) {
                TrackingMode.STEP_BY_STEP -> CompletionsStepper(
                    value = targetPerDay,
                    onValueChange = { targetPerDay = it.coerceIn(1, 50) }
                )
                TrackingMode.CUSTOM_VALUE -> OutlinedTextField(
                    value = targetPerDay.toString(),
                    onValueChange = { text ->
                        targetPerDay = text.filter { it.isDigit() }.toIntOrNull()?.coerceIn(1, 999) ?: targetPerDay
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = darkFieldColors()
                )
            }
            Text(
                "The square will be filled completely when this number is met",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { onSave(name.trim(), selectedIcon, selectedColor, targetPerDay) },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = selectedColor,
                    disabledContainerColor = DarkGray
                )
            ) {
                Text("Save", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = Color.White,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun IconGrid(
    icons: List<ImageVector>,
    selectedIndex: Int,
    accentColor: Color,
    onSelect: (Int) -> Unit
) {
    val columns = 7
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        icons.chunked(columns).forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEachIndexed { colIndex, icon ->
                    val index = rowIndex * columns + colIndex
                    val isSelected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) accentColor.copy(alpha = 0.25f) else DarkSurface)
                            .then(
                                if (isSelected) Modifier.border(1.5.dp, accentColor, CircleShape)
                                else Modifier
                            )
                            .clickable { onSelect(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) accentColor else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorGrid(
    colors: List<Color>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val columns = 7
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        colors.chunked(columns).forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEachIndexed { colIndex, color ->
                    val index = rowIndex * columns + colIndex
                    val isSelected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color)
                            .then(
                                if (isSelected) Modifier.border(2.dp, Color.White, RoundedCornerShape(10.dp))
                                else Modifier
                            )
                            .clickable { onSelect(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackingModeToggle(
    mode: TrackingMode,
    onModeChange: (TrackingMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .padding(4.dp)
    ) {
        listOf(
            TrackingMode.STEP_BY_STEP to "Step By Step",
            TrackingMode.CUSTOM_VALUE to "Custom Value"
        ).forEach { (value, label) ->
            val isSelected = value == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) DarkGray else Color.Transparent)
                    .clickable { onModeChange(value) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = Color.White,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun CompletionsStepper(
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text("$value", color = Color.White, fontWeight = FontWeight.Bold)
            Text(" / Day", color = Color.Gray)
        }
        StepperButton(icon = Icons.Default.Remove) { onValueChange(value - 1) }
        StepperButton(icon = Icons.Default.Add) { onValueChange(value + 1) }
    }
}

@Composable
private fun StepperButton(icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
    }
}

@Composable
private fun darkFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = DarkSurface,
    unfocusedContainerColor = DarkSurface,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color.DarkGray,
    unfocusedBorderColor = Color.DarkGray,
    cursorColor = Color.White
)