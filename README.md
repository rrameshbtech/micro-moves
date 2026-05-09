# MicroMoves (Pausify)

A minimal, elderly-friendly Android health utility that reminds users to take periodic micro-breaks with guided exercises.

## Overview

MicroMoves promotes better health by sending gentle reminders to take small breaks with simple exercises. Whether for office workers sitting all day, elderly individuals needing gentle stretches, or anyone seeking improved wellness, the app provides timely reminders for activities like stretching, breathing exercises, and more.

**Design Philosophy**: "Less, but better" (Dieter Rams). Every UI element serves a functional purpose. The app is optimized for one-handed use, high accessibility, and peaceful user experience.

## Key Features

- **Break Reminders**: Customizable periodic reminders for exercise breaks
- **Guided Exercises**: Auto-advancing slide shows with instructions and images
- **Pause/Resume**: Temporarily pause breaks without disabling them
- **Elderly-Friendly**: Large typography (18sp+), high contrast colors, no hidden menus
- **Customizable Schedule**: Set frequency, active hours, and enable/disable breaks
- **Minimal UI**: Clean design inspired by minimalist principles and accessibility standards

## Tech Stack

- **Language**: Kotlin 2.2.10
- **UI Framework**: Jetpack Compose (BOM 2026.02.01) + Material 3
- **Architecture**: Single-module MVVM (planned: expand with Domain layer)
- **Min SDK**: Android 29 | **Target SDK**: 36
- **Build System**: Gradle Kotlin DSL

### Key Dependencies
- `androidx.compose.*` - UI framework
- `androidx.activity:activity-compose` - Compose activity integration
- `androidx.core:core-ktx` - Android utilities
- `androidx.lifecycle:lifecycle-runtime-ktx` - Lifecycle management
- JUnit4, Espresso - Testing

## Getting Started

### Prerequisites
- Android Studio 2024.1 or later
- JDK 11+
- Android SDK 36+

### Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd code
   ```

2. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

3. **Run on device/emulator**
   ```bash
   ./gradlew installDebug
   ```

4. **Preview UI components** (in Android Studio)
   - Open any composable file with `@Preview` annotation
   - Right-click on preview function → "Show Compose Previews"

## Project Structure

```
code/
├── AGENTS.md                    # AI agent codebase guide
├── README.md                    # This file
├── build.gradle.kts             # Root build config
├── gradle/
│   └── libs.versions.toml       # Centralized dependency versions
├── app/
│   ├── build.gradle.kts
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/rrameshbtech/micromoves/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   └── ui/
│   │   │   │       ├── screens/          # Screen composables (planned)
│   │   │   │       └── theme/
│   │   │   │           ├── Color.kt      # App color palette
│   │   │   │           ├── Theme.kt      # Material3 theme
│   │   │   │           └── Type.kt       # Typography
│   │   │   ├── res/
│   │   │   │   ├── values/strings.xml    # Localized strings
│   │   │   │   └── drawable/             # Icons, images
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                         # Unit tests
│   │   └── androidTest/                  # Instrumented tests
└── docs/
    ├── ai-design-context.md              # Design system & philosophy
    ├── ai-design-dashboard.md            # Dashboard screen specs
    ├── ideation.md                       # Feature roadmap & user flows
    └── screens/                          # Visual mockups
```

## Architecture Overview

### Current State
- Basic Compose app with theme setup (`MainActivity.kt`)
- Material3 color scheme and typography definitions
- No data layer or ViewModels yet

### Planned Screens
1. **Breaks List** - Dashboard showing active breaks with countdown timers
2. **Customize Breaks** - Expand cards to modify frequency and time ranges
3. **Break Execution** - Full-screen auto-advancing slide show during break
4. **New Break Creation** - Add custom exercises with slide-based instructions
5. **Weekly Report** - Wellness rings and 7-day consistency tracking
6. **Initial Setup** - Guided permissions and onboarding flow

### Data Model
**Break**
- `name`: String
- `description`: String (health benefit explanation)
- `frequency`: Int (interval in minutes)
- `activeTimeRange`: Pair<Int, Int> (start/end hours)
- `enabled`: Boolean
- `slides`: List<Slide>

**Slide**
- `instructionText`: String
- `imageUri`: String? (optional image URI)
- `durationMs`: Long (milliseconds to display)

## Design System

### Colors
- **Primary**: Sage Green (`#E8F5E9` light, `#558B2F` dark)
- **Background**: Off-white (`#F9F9F9`)
- **Text**: High-contrast charcoal (`#2D2D2D`)
- **Muted**: Light gray-green for paused/disabled states

### Typography
- **Minimum font size**: 18sp for body text (elderly accessibility)
- **Font family**: Inter or SF Pro (system sans-serif as fallback)
- **All clickable targets**: Minimum 60dp height/width

### Spacing & Layout
- **Card spacing**: 20px
- **Horizontal margins**: 24px
- **Corner radius**: 16dp
- **One-handed zone**: All primary actions within bottom 60% of screen

## Development Workflow

### Build Variants
```bash
# Debug APK
./gradlew assembleDebug

# Release APK (currently not minified)
./gradlew assembleRelease

# Install and run
./gradlew installDebug

# Connected device tests
./gradlew connectedAndroidTest
```

### Testing
```bash
# Unit tests
./gradlew test

# UI/Instrumented tests
./gradlew connectedAndroidTest
```

### Common Commands
```bash
# Clean build
./gradlew clean

# Check for Kotlin/Compose issues
./gradlew lint

# Update dependencies
./gradlew dependencyUpdates
```

## Accessibility Requirements

All screens must meet these criteria:
- ✅ Typography ≥18sp for body text
- ✅ Touch targets ≥60dp height/width
- ✅ WCAG AA contrast ratio for all text (charcoal on off-white)
- ✅ No hidden menus or complex gestures
- ✅ Semantic labels for all icon buttons (`contentDescription`)
- ✅ Screen reader compatibility

**Test accessibility**: Use Android Accessibility Scanner or TalkBack screen reader on device.

## Next Steps

See `AGENTS.md` for AI agent guidelines and `docs/ideation.md` for complete feature scope.

### Immediate Priorities
1. **Color System**: Update `Color.kt` with sage green palette (referenced in design docs)
2. **Typography**: Increase `bodyLarge` to 18sp in `Type.kt`, add `headlineMedium`, `labelLarge`
3. **First Screen**: Implement `BreaksListScreen` in `ui/screens/` with mock data
4. **Data Layer**: Define Break and Slide data classes, set up mock repository

### Future Integrations
- Room database for persisting breaks
- AlarmManager for background notifications
- Full-screen intents for Android 12+
- Exercise image caching with Coil

## Contributing

When contributing:
1. Follow Kotlin coding conventions (see `gradle.properties`: `kotlin.code.style=official`)
2. Always include `@Preview` annotations for UI components
3. Add user-facing strings to `res/values/strings.xml`
4. Test accessibility with Android Accessibility Scanner
5. Verify designs match `/docs/ai-design-*.md` specifications

## License

Refer to `LICENSE` file for licensing information.

## Contact

Email: rrameshbtech@gmail.com

---

**Last Updated**: May 9, 2026

