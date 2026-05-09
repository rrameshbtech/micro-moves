# AGENTS.md: MicroMoves (Pausify) Codebase Guide

## Project Overview

**MicroMoves** ("Pausify") is a minimal, elderly-friendly Android health utility that reminds users to take periodic micro-breaks with guided exercises. The app is built with **Kotlin + Jetpack Compose** on a single-module Android architecture.

### Core Philosophy
- **Dieter Rams' "Less, but better"**: Every UI element must serve functional purpose
- **One-handed operation**: All primary actions within bottom 60% of screen
- **Accessibility-first**: Elderly users (18pt+ font, high contrast, no hidden menus)
- **Peaceful & minimal**: Muted colors (sage green `#E8F5E9` or sky blue `#E3F2FD`), off-white backgrounds

## Architecture Patterns

### Two-Tier Screen Hierarchy
Planned screens follow a primary flow:
1. **Breaks List** (main dashboard): Shows active breaks with countdown timers + Pause/Resume CTAs
2. **Customize Breaks**: Expand cards to modify frequency, time ranges, enable/disable
3. **Break Execution**: Auto-advancing slide show during break (image + short text + timing)
4. **New Break Creation**: Create custom exercises with slide-based definitions

Currently, only base theme setup exists in `MainActivity.kt`. Future screens should be Composable functions in `ui/screens/` directory following this pattern:
```kotlin
@Composable
fun BreaksListScreen(modifier: Modifier = Modifier) { ... }

@Preview(showBackground = true)
@Composable
fun BreaksListScreenPreview() { ... }
```

### Data Model (Implied from Ideation)
**Break** object should contain:
- `name` (string)
- `description` (string explaining health benefit)
- `frequency` (interval in minutes)
- `activeTimeRange` (start/end hours)
- `enabled` (boolean)
- `slides` (list of exercise steps)

**Slide** object should contain:
- `instructionText` (string)
- `imageUri` (optional, for pictures of exercise pose)
- `durationMs` (milliseconds to display)

## Build & Development

### Gradle Setup
- **Single-module**: `/app/` contains all source code
- **Compose BOM**: `2026.02.01` (Feb 2026 release)
- **Min SDK**: Android 29; **Target SDK**: 36
- **Language**: Kotlin with Compose function signatures (lambdas in trailing position)
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

### Dependencies (From `libs.versions.toml`)
- **Material3**: For design system with custom colors
- **Compose Activity**: Entry point for Compose-based activities
- **AndroidX Core Ktx**: Core Android utilities
- **Espresso + JUnit**: Testing framework (basic setup, expand as needed)

## UI & Design Conventions

### Color System
Currently using Material3 defaults (purples). **TODO**: Replace with brand colors in `Color.kt`:
```kotlin
// Sage Green palette
val SageLight = Color(0xFFE8F5E9)
val SageMedium = Color(0xFFC8E6C9)
val SageDark = Color(0xFF558B2F)

// Neutrals
val OffWhite = Color(0xFFF9F9F9)
val Charcoal = Color(0xFF2D2D2D)
```

Update `Theme.kt` to use these in `lightColorScheme()` and `darkColorScheme()`.

### Typography
- **Body text**: Minimum 18sp for elderly accessibility (see `Type.kt`)
- **Font**: Inter or SF Pro preferred (currently using FontFamily.Default)
- Already defined `bodyLarge` at 16sp—increase and add `headlineMedium`, `labelLarge` for buttons

### Component Patterns
1. **Break Cards**: Use Material3's `Card()` composable with `--card` background + `--shadow` elevation
   - Active state: Full opacity, timer in primary color, "Pause" button
   - Paused state: 60% opacity, "Paused for X cycles" text, "Resume" button
2. **Buttons**: 60px minimum height, no aggressive gradients, use soft borders/shadows
3. **Spacing**: 20px between cards, 24px horizontal margins, 16px corner radius

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

## Resource Structure

```
app/src/main/
├── java/com/rrameshbtech/micromoves/
│   ├── MainActivity.kt                 # Entry point
│   └── ui/
│       ├── screens/                    # (TODO) Screen composables
│       └── theme/
│           ├── Color.kt                # App color palette
│           ├── Theme.kt                # Material3 theme config
│           └── Type.kt                 # Typography definitions
└── res/
    ├── values/strings.xml              # All user-facing text
    └── drawable/                       # Icons, images (SVG preferred)
```

**Texting strings**: Add all user-facing text to `res/values/strings.xml` (enables future localization for elderly users in multiple languages).

## Critical Integration Points

### Break Notifications (Future)
- Android requires explicit alarm/notification permissions (requested at setup)
- Will need `AlarmManager` for periodic break triggers
- Full-screen intent supported on Android 12+ for interrupting work

### Exercise Data Format (Future)
- Slides with images should store URIs in local cache, not embed large bitmaps
- Time-to-show per slide should be configurable per exercise

### Accessibility APIs (Future)
- Screen reader compatibility: Wrap all icon-only buttons with `contentDescription`
- Voice command integration placeholder for future

## Common Pitfalls & Patterns

1. **Hard-coded dimensions**: Use `rememberNavController()` + responsive layouts instead of fixed widths
2. **Dark mode support**: Always test with `darkTheme = true` in `@Preview` (elderly users often prefer high contrast)
3. **Compose recomposition**: Avoid passing lambdas without `remember` to nested composables (causes unnecessary recreation)
4. **State loss**: Don't rely on Activity member variables for UI state—use `remember` or `ViewModel`

## Design Documentation References

- **Visual Language & Atmosphere**: `/docs/ai-design-context.md` (color, typography, interaction philosophy)
- **Dashboard Mock Design**: `/docs/ai-design-dashboard.md` (Breaks List screen specifications with card states)
- **Feature Ideation**: `/docs/ideation.md` (all planned screens, data model, user flows)

---

**Next Steps for New Developers**: 
1. Read `/docs/ideation.md` for full feature scope
2. Review `/docs/ai-design-dashboard.md` for visual specifications
3. Create `ui/screens/BreaksListScreen.kt` with Compose preview
4. Implement Break data model and mock data for preview testing

