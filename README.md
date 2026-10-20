# ✅ Habit Tracker

A clean, dark-themed Android habit tracker with **GitHub-style heatmaps** and a **resizable home screen widget**. Create habits with your own icon and color, tick them off daily, and see your consistency at a glance, both in the app and right on your home screen.

Built with **Kotlin**, **Jetpack Compose**, and **Jetpack Glance**.

---

## ✨ Features

- **Heatmap per habit**: each habit card shows a 20-week grid (140 days) where every square is a day. The more you complete, the more intense the color.
- **Daily targets**: set how many times per day a habit should be done (1 to 999). Each tap adds one completion, and the square fills in as you approach your target. Tapping again after reaching the target resets that day.
- **Backfill missed days**: tap any past square (or today's) in the heatmap to toggle that specific day. Future days are inert.
- **Customizable habits**: pick from 28 icons and 21 colors, with a live preview while you create the habit. Choose between *Step By Step* (stepper) or *Custom Value* (type a number) for the daily target.
- **Home screen widget** (4×1 by default, resizable both ways):
  - Shows all your habits with their real icons and colors.
  - Adapts the number of day-columns and week-rows to the widget's size.
  - Tap any day cell directly on the widget to log or undo a completion.
- **App and widget stay in sync**: both read and write the same repository, so a check-off in one shows up in the other immediately.
- **Delete with confirmation**: long-press a habit card to delete it along with its history.
- **Persistent storage**: habits and their full completion history survive app kills and device reboots. A fresh install starts empty, with no demo data.
- **Local-only**: no account, no network access, no permissions required.

## 🛠 Tech Stack

| Area | Technology |
| --- | --- |
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose (BOM 2024.09.00) + Material 3, Material Icons Extended |
| Widget | Jetpack Glance 1.1.1 (`glance-appwidget`) |
| Persistence | Jetpack DataStore (Preferences) 1.1.1, with habits serialized as JSON via `org.json` |
| Build | Gradle (Kotlin DSL), Android Gradle Plugin 8.7.3 |
| Min / Target / Compile SDK | 26 / 35 / 35 |

## 📁 Project Structure

```
app/src/main/java/com/example/habittracker/
├── MainActivity.kt                 # Navigation (Dashboard ↔ Add Habit), delete dialog
├── data/
│   ├── HabitRepository.kt          # Single source of truth shared by app + widget
│   └── HabitStorage.kt             # DataStore save/load (JSON serialization)
├── ui/
│   ├── components/
│   │   ├── HabitCard.kt            # Habit card + HabitUiModel
│   │   ├── Heatmap.kt              # GitHub-style contribution grid
│   │   ├── HabitIcons.kt           # Icon palette for the picker
│   │   ├── IconRegistry.kt         # Icon ↔ string id mapping for persistence
│   │   └── IconRasterizerEffect.kt # Renders icons to bitmaps for the widget
│   ├── screens/
│   │   └── AddHabitScreen.kt       # New habit form (icon, color, target)
│   └── theme/                      # Colors (incl. habit color palette), Theme, Type
└── widget/
    ├── HabitWidget.kt              # Glance widget layout + tap-to-toggle action
    ├── HabitWidgetReceiver.kt      # App widget receiver
    ├── HabitWidgetUpdater.kt       # Refreshes all placed widgets
    └── IconBitmapCache.kt          # Memory + disk cache of rasterized icons
```

## ⚙️ How It Works

- **One repository for everything.** `HabitRepository` holds the habit list as Compose state. Every change (add, delete, toggle) goes through it and is saved to disk right after, so the UI and the widget never drift apart.
- **Storage.** `HabitStorage` writes the whole habit list as a JSON string into DataStore. Icons can't be serialized directly, so each is stored by name through `IconRegistry` and looked up again on load.
- **Widget icons.** Glance can't draw Compose `ImageVector`s, so `IconRasterizerEffect` renders each icon and color combination off-screen to a bitmap while the app is open. `IconBitmapCache` keeps them in memory and in the cache directory, so the widget can still draw real icons after the app process is gone.
- **Responsive widget.** The widget uses exact sizing and recalculates how many day-columns (4 to 21) and week-rows (up to 3 per habit) fit every time it's drawn.

## 🚀 Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (a recent version that supports AGP 8.7.x)
- JDK 11 or newer
- An Android device or emulator running **Android 8.0 (API 26)** or higher

### Build and run

```bash
# Clone the repository
git clone https://github.com/<your-username>/HabitTracker.git
cd HabitTracker
```

1. Open the project in Android Studio and let Gradle sync.
2. Select a device or emulator.
3. Click **Run ▶**, or build from the command line:

```bash
./gradlew assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

### Add the widget

1. Long-press an empty area of your home screen.
2. Open **Widgets** and find **Habit Tracker**.
3. Drag it onto your home screen and resize it. A taller widget shows more weeks per habit.
4. Tap any day cell to log or undo a completion.

## 🎮 How to Use

| Action | Gesture |
| --- | --- |
| Add a habit | Tap **+** in the top bar |
| Log a completion for today | Tap the colored check button on the habit card |
| Log or undo a specific day | Tap that square in the heatmap (or widget) |
| Delete a habit | Long-press its card, then confirm |

## 🗺 Ideas for the Future

- Streak counters and statistics
- Habit descriptions saved and shown on the card (the field exists on the New Habit screen, but its value isn't saved yet)
- Reminder notifications
- Edit existing habits
- Reordering habits
- Light theme and data export

## 🤝 Contributing

Contributions, issues, and feature requests are welcome. Feel free to open an issue or submit a pull request.

## 📄 License

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
