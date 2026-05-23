# AGENTS.md: MicroMoves Codebase Guide

## Project Overview

**MicroMoves** is a minimal, elderly-friendly Android health utility that reminds users to take periodic micro-breaks with guided exercises. The app is built with **Kotlin + Jetpack Compose** using a single-module MVVM architecture.

## AI Guidelines
- Always show plan and reasoning before code.

### Core Philosophy
To maintain the product's integrity, every screen must adhere to these functional constraints:
1. **Dieter Rams' "Less, but better"**: Every UI element must serve a functional purpose. If a break is paused, the UI must look "asleep" (muted/dimmed).
2. **One-Handed Rule**: Primary interactive elements (Buttons, Toggles, Sliders) MUST be in the bottom 60% of the screen.
3. **Elderly-Friendly Constraints**:
   - **Typography**: Minimum `18sp` for body text.
   - **Touch Targets**: Minimum `60dp` height/width for all clickable elements.
   - **Contrast**: High contrast charcoal (`#2D2D2D`) on off-white (`#F9FBF9`) following WCAG AA standards.
   - **No hidden menus**: All navigation visible and tap-accessible.

## Tech Stack & Dependencies
- **Language**: Kotlin (Strict use of Coroutines and `Flow` for reactive programming).
- **Minimum SDK**: API 26 (Android 8.0) | **Target SDK**: 35+
- **UI Toolkit**: Jetpack Compose + Material 3.
- **AndroidX KTX**: Always use KTX extensions (`core-ktx`, `lifecycle-viewmodel-ktx`, `room-ktx`) for idiomatic Kotlin integration.
- **Image Loading**: Coil (Coroutine Image Loader) for rendering compressed WebP exercise slides.
- **Local Database**: Room (SQLite wrapper) for persisting `Break` and `Slide` entities.
- **Scheduling**: `AlarmManager` (Exact Alarms) with `BroadcastReceiver` to handle background triggers reliably.
- **Architecture**: Clean Architecture with MVI (Model-View-Intent) / MVVM presentation pattern.

## Testing Stack (Strictly Kotlin-Native)
When writing tests, strictly use the following libraries. Do not use legacy Java testing tools like Mockito.
- **Unit Testing**: JUnit4, `MockK` (for mocking domain/system classes), `kotlinx-coroutines-test` (for dispatchers), and `Turbine` (for testing StateFlows).
- **UI & Integration Testing**: Compose UI Test (`ui-test-junit4`), Room Testing (`room-testing`), and Espresso only when necessary for system-level interactions.

## Project Structure

```
app/src/main/
├── java/com/rrameshbtech/micromoves/
│   ├── MainActivity.kt                 # Entry point
│   ├── data/                           # Break and Slide models
|   ├── domain/                         # Pure domain objects and business logic (No Android dependencies)
│   ├── viewmodel/                      # Logic for timers, triggers, and state
│   └── ui/
│       ├── components/                 # Reusable BreakCards, Buttons, ProgressRings
│       ├── screens/                    # Dashboard, Customize, Execution, Report
│       └── theme/
│           ├── Color.kt                # Brand colors defined above
│           ├── Theme.kt                # Material3 theme config
│           └── Type.kt                 # 18sp+ Typography definitions
└── res/
    ├── values/strings.xml              # User-facing text (for localization)
    └── drawable/                       # Icons (SVG/VectorDrawables)
```

## Build & Development

### Gradle Setup
- **Single-module**: `/app/` contains all source code
- **Compose BOM**: `2026.02.01` (Feb 2026 release)
- **Version code**: Currently 1.0

### Key Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Run tests (currently minimal)
./gradlew test

# Build and run on device
./gradlew installDebug
./gradlew connectedAndroidTest

# Preview Compose UI in IDE
# Right-click @Preview function → "Show Compose Previews"
```

## Architecture Patterns

### Core Tech Principles
- Follow Domain-Driven Design (DDD) principles: separate domain logic from Android framework dependencies.
- Use `ViewModel` for UI state management and business logic.
- Use `StateFlow` for reactive UI updates and `SharedFlow` for one-time events.
- Follow YAGNI, KISS, DRY, object calisthenics, and SOLID principles rigorously.

### Two-Tier Screen Hierarchy
Planned screens follow a primary flow:
1. **Breaks List (Dashboard)**: Shows active breaks with countdown timers + Pause/Resume CTAs.
2. **Customize Breaks (Editor)**: Expand cards to modify frequency, time ranges, and visual slide definitions.
3. **Break Execution (Immersive)**: Full-screen auto-advancing slide show during a break.
4. **New Break Creation**: Create custom exercises with slide-based definitions
5. **Weekly Report (Reflection)**: Shows wellness rings and 7-day consistency bars.
6. **Initial Setups**: Show step by step guide for the user who open app for first time to provide required app access and and latest in "Customize Breaks" screen. 

Currently, only base theme setup exists in `MainActivity.kt`. Future screens should be Composable functions in `ui/screens/` directory following this pattern:
```kotlin
@Composable
fun BreaksListScreen(modifier: Modifier = Modifier) { ... }

@Preview(showBackground = true)
@Composable
fun BreaksListScreenPreview() { ... }
```

### Data Model (Implied from Ideation)
#### Base model hierarchy
Slide ->  image URI, time-to-show, description
Exercise -> Name, description, List<Slide>, total duration (derived from slides)
RoutineStep -> Exercise, pauseAfterMs (boolean)
BreakRoutine -> List<RoutineStep>, total duration (derived)
BreakState -> Active, Paused, PausedForOccurrence
BreakSchedule -> Frequency (e.g. every 30 mins), Time Range (e.g. 9am-5pm), Trigger Type (e.g. exact alarm)
Break -> name, BreakSchedule, BreakRoutine, BreakState, nextTriggerTime (derived from schedule + state)

## Theme & Styling Constants (`Theme.kt` & `Color.kt`)

Translate the minimalist design system into Compose using these specific values:

```kotlin
// Colors
val Background = Color(0xFFF9FBF9) // Soft off-white to reduce eye strain
val Primary = Color(0xFF6A9C78) // Sage Green for active states/timers
val Secondary = Color(0xFFE9EFEC) // Muted gray-green for secondary actions
val Muted = Color(0xFFF1F3F1) // For paused/disabled states
val Foreground = Color(0xFF2D2D2D) // High-contrast charcoal for text

// Typography (`Type.kt`)
// Base all body text at 18sp minimum (Inter or system sans-serif)

// Dimensions
val CardCornerRadius = 16.dp
val TouchTargetMinHeight = 60.dp
val StandardPadding = 24.dp
val CardSpacing = 20.dp
val SubtleElevation = 4.dp // Low-alpha elevation for a tactile look
```


### Typography
- **Body text**: Minimum 18sp for elderly accessibility (see `Type.kt`)
- **Font**: Inter or SF Pro preferred (currently using FontFamily.Default)
- Already defined `bodyLarge` at 16sp—increase and add `headlineMedium`, `labelLarge` for buttons

### Layout Container
- All screens wrapped in `MicroMovesTheme { ... }`
- Use `Scaffold()` for consistent top bar + bottom action areas
- Implement **fixed bottom button** for "Manage Breaks" (width: 100% - padding, height: 60px)

## Key Development Practices

### Composable Naming & Previews
- Name UI composables after their screen/component: `BreaksListScreen`, `BreakCard`, `PauseButton`
- Always include `@Preview` annotations for rapid visual feedback during development
- Use `showBackground = true` for isolated component testing

### State Management (Future)
When break state and timers are implemented:
- Use `MutableState<>` for local UI state (pause button toggles)
- Consider `ViewModel` for persistent break data (frequency, schedule)
- Use `remember { }` to preserve state across recompositions

### Accessibility Priorities
- **Content descriptions**: All buttons must have `contentDescription` param
- **Text sizes**: Never below 18sp body text
- **Color contrast**: Verify all text against WCAG AA standards (use accessibility scanner)
- **Touch targets**: Minimum 60px height for all interactive elements
- **No hidden menus**: All navigation visible and tap-accessible

### Testing Patterns
- Place screenshot/manual verification tests in `androidTest/`
- Use `@Preview` for UI validation before device testing
- Espresso tests for user flows (pause break, enable/disable)

**Texting strings**: Add all user-facing text to `res/values/strings.xml` (enables future localization for elderly users in multiple languages).

## Critical Integration Points

### Break Notifications (Future)
- Android requires explicit alarm/notification permissions (requested at setup)
- Will need `AlarmManager` for periodic break triggers
- Full-screen intent supported on Android 12+ for interrupting work

### Exercise Data Format (Future)
- Slides with images should store URIs in local cache, not embed large bitmaps
- Time-to-show per slide should be configurable per exercise

## Common Pitfalls & Patterns

1. **Hard-coded dimensions**: Use `rememberNavController()` + responsive layouts instead of fixed widths
2. **Dark mode support**: Always test with `darkTheme = true` in `@Preview` (elderly users often prefer high contrast)
3. **Compose recomposition**: Avoid passing lambdas without `remember` to nested composables (causes unnecessary recreation)
4. **State loss**: Don't rely on Activity member variables for UI state—use `remember` or `ViewModel`
5. **Accessibility**: Screen reader compatibility is non-negotiable. Wrap all icon-only buttons with clear contentDescription params.

## Design Documentation References

- **Visual Language & Atmosphere**: `/docs/ai-design-context.md` (color, typography, interaction philosophy)
- **Mock Screen Designs**: `/docs/screens` (All planned screens with detailed visual specifications)
- **Feature Ideation**: `/docs/ideation.md` (all planned screens, data model, user flows)

---

**Next Steps for New Developers**: 
1. Read `/docs/ideation.md` for full feature scope
2. Review `/docs/ai-design-dashboard.md` for visual specifications
3. Create `ui/screens/BreaksListScreen.kt` with Compose preview
4. Implement Break data model and mock data for preview testing

