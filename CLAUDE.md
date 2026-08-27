# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MicroMoves (Pausify) is a minimal, elderly-friendly Android app that reminds users to take periodic micro-breaks with guided exercises. Single-module Kotlin + Jetpack Compose app using MVVM.

**Design philosophy** (applies to every screen, not just documentation): Dieter Rams' "less, but better." Concretely:
- Body text minimum 18sp, touch targets minimum 60dp.
- High-contrast charcoal-on-off-white palette (see `ui/theme/Color.kt`), WCAG AA.
- Primary actions (buttons, toggles, sliders) live in the bottom 60% of the screen for one-handed use.
- No hidden menus — all navigation visible and tap-accessible.
- A paused break must look visibly "asleep" (dimmed/muted), not just say so — see `PausedBreakCard` in `BreaksListScreen.kt` for the pattern (`.alpha(0.6f)` + muted color set).

## Build & Test Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Build and install on connected device/emulator
./gradlew test                   # Run JVM unit tests (app/src/test)
./gradlew connectedAndroidTest   # Run instrumented tests (app/src/androidTest)
./gradlew lint                   # Static analysis
./gradlew clean
```

Run a single JVM test class: `./gradlew test --tests "com.rrameshbtech.micromoves.ExampleUnitTest"`.

Compose previews (`@Preview` functions) are the fastest feedback loop for UI work — open the file in Android Studio and use "Show Compose Previews" rather than deploying to a device for layout changes.

## Architecture

### Data model (`data/Models.kt`, one `@Entity` per file under `data/`)

Exercises are a built-in catalog with a stable identity (referenced from many breaks and from report history); a Break is a user-customized ordered bundle of catalog exercises. Domain hierarchy, from smallest to largest:

```
Slide             -> imageUri, durationMs, description (one step of an exercise)
Exercise          -> Room @Entity: id, name, description, List<Slide> (JSON column), suggestedSchedule (embedded BreakSchedule)
DaysOfWeek        -> value object wrapping Set<java.time.DayOfWeek>; stored as an Int bitmask
BreakSchedule     -> frequencyMinutes, activeStartHour, activeEndHour, daysOfWeek
RoutineStep       -> Room @Entity (join table): breakId, exerciseId, position, pauseAfterStep — a break's ordered routine, persisted rather than embedded
BreakState        -> sealed class: Active | PausedForOccurrences(occurrences) | PausedUntil(timestampMillis)
Break             -> Room @Entity: id, name, schedule (embedded), enabled, state, timestamps; nextTriggerTimeInMins(now) is computed on the fly from schedule, not stored
BreakOccurrence   -> Room @Entity: id, breakId, triggeredAt — one row per time a break actually fires (report audit-log parent)
ExerciseOutcome   -> sealed class: Completed | Skipped | Paused
ExerciseOccurrence-> Room @Entity: id, breakOccurrenceId, exerciseId, position, outcome, durationMs — one row per exercise within an occurrence (report audit-log child)
```

`BreakRoutine`/`ResolvedRoutineStep` in `Models.kt` are plain (non-persisted) read-models — `BreakRoutine(breakItem, steps: List<ResolvedRoutineStep>)` is the hydrated join of a `Break` with its ordered `RoutineStep`s resolved to full `Exercise` objects, built by the `MicroMovesDatabase.getBreakRoutine(breakId)` extension function (in `MicroMovesDatabase.kt`) rather than stored anywhere. `enabled` (persistent on/off) is intentionally separate from `BreakState` (temporary, auto-resuming pause conditions) — a disabled break and a paused break both render as "asleep" in the UI, but only a paused one has a resume condition.

### Persistence (`data/local/`)

- `MicroMovesDatabase` is a singleton Room database, version 2, with `Break`, `Exercise`, `RoutineStep`, `BreakOccurrence`, and `ExerciseOccurrence` as entities (`fallbackToDestructiveMigration`, so schema changes during development don't need migrations yet — revisit once there's real user data to preserve).
- One DAO per entity: `BreakDao`, `ExerciseDao`, `RoutineStepDao`, `BreakOccurrenceDao`, `ExerciseOccurrenceDao` (all in `Daos.kt`). `BreakDao`/`ExerciseDao` expose `Flow<List<...>>` for reactive reads plus suspend functions for writes.
- `MicroMovesDBConverters` serializes `BreakState` to a custom string format (`ACTIVE`, `PAUSED_FOR_OCCURRENCES:<n>`, `PAUSED_UNTIL:<millis>`), `ExerciseOutcome` similarly (`COMPLETED`/`SKIPPED`/`PAUSED`), `DaysOfWeek` to an `Int` bitmask, and `List<Slide>` to JSON via Gson (on `Exercise`, not on `Break` — routines are no longer embedded blobs). Any new field added to `BreakState`'s or `ExerciseOutcome`'s subclasses must be reflected in both the `from*`/`to*` converter functions.
- Weekly-report aggregation (time spent per exercise, paused/skipped stats) should read `ExerciseOccurrenceDao.getEntriesSince(sinceMillis)` and aggregate in Kotlin over the typed `ExerciseOutcome` — deliberately not via `GROUP BY`/`WHERE outcome = '...'` in SQL, since that would hardcode the converter's string encoding into a query string with no compiler check.
- On first database creation, `MicroMovesDatabase.SeedCallback` fires `DatabaseSeeder.seed()`, which reads `app/src/main/assets/exercises_catalog.json` (built-in exercise catalog: id, slides, suggested schedule) then `app/src/main/assets/init_breaks.json` (starter breaks: schedule + ordered `exerciseIds`) and inserts both, wiring up `RoutineStep` rows to link them. Update those JSON files (not code) to change default seed data.

### UI / ViewModel

- `BreaksListViewModel` (`AndroidViewModel`) exposes DAO's `Flow<List<Break>>` as a `StateFlow` via `stateIn(WhileSubscribed(5_000))` — the standard pattern for screen-level ViewModels reading from Room in this app.
- `BreaksListScreen.kt` splits into a stateful `BreaksListScreen` (owns the ViewModel) and a stateless `BreaksListContent`/card composables that take plain data — keep this split for new screens so previews can pass mock data without a ViewModel or database.
- Theme constants live in `ui/theme/Color.kt` (`*Light` suffixed sage-green palette) and `ui/theme/Type.kt` (typography). `Theme.kt` still carries a `DarkColorScheme` and legacy Purple/Pink color constants from the default Compose template — these are unused by the app's actual design and dynamic color is enabled by default, so don't assume the sage palette is what renders on Android 12+ devices unless `dynamicColor = false`.

### What's not built yet

Only the Breaks List screen and its Room-backed data layer exist today. The `Exercise` catalog, `RoutineStep` ordering, and `BreakOccurrence`/`ExerciseOccurrence` report tables exist in the schema and are seeded, but have no UI and no write-path yet — nothing currently inserts a `BreakOccurrence`/`ExerciseOccurrence` row, since that requires a break-execution flow that doesn't exist. `docs/ideation.md` describes the full planned flow (Customize Breaks, Break Execution slideshow, New Break creation, Weekly Report, initial permissions setup) and `docs/screens/*.html` has visual mockups for those screens — check both before building a new screen so it matches the intended design rather than inventing layout from scratch. Notification/scheduling (`AlarmManager`, exact alarms, full-screen intents) and Coil-based image loading are dependencies already in `build.gradle.kts` but have no code using them yet.

## Key References

- `docs/ideation.md` — full feature scope, screen-by-screen behavior, data model rationale.
- `docs/ai-design-context.md`, `docs/ai-design-dashboard.md` — visual/interaction design system.
- `docs/screens/*.html` — mockups for each planned screen.
