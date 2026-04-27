# Architecture — Nouri Diet Companion

This document is the definitive guide to Nouri's system design. All developers and Claude Code sessions must follow these conventions. Any architectural decision change requires an update to this document.

---

## Module Structure

```
Nouri/
├── androidApp/                        # Android entry point (Activity, Application)
├── iosApp/                            # iOS entry point (Swift/Xcode)
├── shared/
│   ├── commonMain/
│   │   ├── data/
│   │   │   ├── datasource/            # Supabase + SQLDelight clients
│   │   │   │   ├── remote/            # SupabaseDataSource implementations
│   │   │   │   └── local/             # SQLDelightDataSource implementations
│   │   │   ├── repository/            # RepositoryImpl — bridges remote + local
│   │   │   └── model/                 # DTOs (remote) + local DB models
│   │   ├── domain/
│   │   │   ├── repository/            # Repository interfaces (no implementation)
│   │   │   ├── usecase/               # All use cases — one class per use case
│   │   │   └── model/                 # Domain models (pure Kotlin, no platform deps)
│   │   └── presentation/
│   │       ├── base/                  # BaseViewModel, State, Intent, Effect
│   │       └── shared/                # NouriTheme, shared Composables, design tokens
│   ├── mobileMain/                    # Shared mobile-only code (Android + iOS)
│   ├── androidMain/                   # Android platform implementations
│   ├── iosMain/                       # iOS platform implementations
│   └── wasmJsMain/                    # Web (WASM) platform implementations
└── supabase/
    ├── migrations/                    # Versioned schema migrations
    └── functions/                     # Supabase Edge Functions
```

### Dependency rules

- `domain` has **zero** platform or framework dependencies — pure Kotlin only
- `data` depends on `domain` (implements its interfaces)
- `presentation` depends on `domain` (consumes use cases)
- `presentation` must **never** depend on `data` directly
- Platform modules (`androidMain`, `iosMain`, `wasmJsMain`) depend on `commonMain`

---

## MVI Architecture

Nouri uses MVI (Model–View–Intent). All screens follow this pattern without exception.

### Contracts

Each screen defines four types in its presentation layer:

```kotlin
// What the UI renders
data class ExampleState(
    val isLoading: Boolean = false,
    val data: List<Item> = emptyList(),
    val error: String? = null
)

// User actions sent from the UI to the ViewModel
sealed interface ExampleIntent {
    data class LoadData(val id: String) : ExampleIntent
    data object Refresh : ExampleIntent
}

// Internal ViewModel actions (maps Intent → UseCase call)
sealed interface ExampleAction {
    data class FetchItems(val id: String) : ExampleAction
}

// One-shot events emitted to the UI (navigation, toasts, dialogs)
sealed interface ExampleEffect {
    data class NavigateTo(val route: String) : ExampleEffect
    data class ShowSnackbar(val message: String) : ExampleEffect
}
```

### MVI flow diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                            UI Layer                             │
│   Composable observes State, dispatches Intent on events    │
└──────────────────────────┬──────────────────────────────────────┘
                           │ Intent
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                         ViewModel                               │
│   Receives Intent → maps to Action → calls UseCase              │
│   Emits State (StateFlow) + Effect (Channel)                │
└────────────┬────────────────────────────────────────────────────┘
             │ UseCase call
             ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Domain Layer                              │
│   UseCase — single responsibility, no platform deps             │
│   Calls Repository interface                                    │
└────────────┬────────────────────────────────────────────────────┘
             │ Repository interface call
             ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Data Layer                               │
│   RepositoryImpl — orchestrates remote + local data sources     │
│   ├── RemoteDataSource (Supabase)                               │
│   └── LocalDataSource  (SQLDelight)                             │
└─────────────────────────────────────────────────────────────────┘
```

State and effects flow back:

```
DataSource → RepositoryImpl → UseCase → ViewModel → State / Effect → UI
```

### Side effects

`Effect` is a one-shot event delivered via a `Channel`. Used for:
- Navigation
- Snackbars / toasts
- Dialog triggers
- System actions (open file picker, etc.)

Never put navigation logic directly in a Composable. Always trigger via `Effect`.

### Concrete example — logging a meal slot

1. Patient taps "Followed" on a meal slot → UI dispatches `LogMealIntent.Submit(slotId, status=FOLLOWED)`
2. `MealLogViewModel` receives intent → maps to `LogMealAction.Save`
3. `LogMealUseCase` is called with slot ID and status
4. `ComplianceRepository.saveLog()` is called
5. `ComplianceRepositoryImpl` writes to SQLDelight immediately, then syncs to Supabase
6. Use case returns `Result.Success`
7. ViewModel emits new `State` with updated slot status
8. UI re-renders the slot as "Followed" ✓

---

## Offline-First Data Flow

### Write path

```
UI → ViewModel → UseCase → RepositoryImpl
                                ├── SQLDelight.insert()   ← immediate, optimistic
                                └── Supabase.upsert()     ← async, best-effort
```

Writes go to SQLDelight first. The local record is marked `synced = false`. Supabase sync is attempted immediately if online, or deferred if offline.

### Read path

```
RepositoryImpl
    ├── emit SQLDelight data immediately  ← user sees data instantly
    └── fetch Supabase in background      ← update local cache if newer
```

Repositories return a `Flow` from SQLDelight. Supabase responses update the local cache, which triggers the Flow automatically.

### Sync on reconnect

```
ConnectivityObserver (platform-specific)
    └── emits online event
            └── SyncUseCase.syncPendingLogs()
                    └── fetches all SQLDelight records where synced = false
                            └── upserts to Supabase in batch
                                    └── marks records synced = true
```

---

## Error Handling

### Error types (domain layer)

```kotlin
sealed interface NouriError {
    data object NetworkError : NouriError
    data object AuthError : NouriError
    data class NotFoundError(val entity: String) : NouriError
    data class ValidationError(val field: String, val message: String) : NouriError
    data class UnknownError(val cause: Throwable?) : NouriError
}
```

### BaseViewModel error handling

`BaseViewModel` catches errors from use cases and maps them to `Effect`:

```
UseCase throws / returns error
    └── BaseViewModel.handleError(error)
            ├── NetworkError     → Effect.ShowSnackbar("No internet connection")
            ├── AuthError        → Effect.NavigateTo(Routes.Login)
            ├── NotFoundError    → State.error = "Not found"
            ├── ValidationError  → State.fieldError = message
            └── UnknownError     → Effect.ShowSnackbar("Something went wrong")
```

### When to use what

| Scenario | Response |
|---|---|
| Temporary network issue | Snackbar — silent retry in background |
| Auth expired | Navigate to login |
| Data not found | Full error state in screen |
| Form validation | Inline field error |
| Unrecoverable error | Error state with retry button |

---

## Navigation

### Structure

- **Mobile (Android + iOS):** Bottom navigation bar, role-aware tabs
  - Patient: Home, Meal Plan, Log, Progress, Appointments
  - Nutritionist: Home, Patients, Appointments
- **Web:** Sidebar navigation with URL-based routing

### Conventions

- All routes defined in a central `Routes` object in `commonMain`
- Navigation is always triggered via `Effect` from the ViewModel — never directly from a Composable
- Decompose 3.1.0 handles back stack and component lifecycle
- Deep link routes defined alongside screen routes in `Routes`

```kotlin
object Routes {
    const val HOME = "home"
    const val MEAL_PLAN = "meal_plan"
    const val LOG = "log"
    const val APPOINTMENT_DETAIL = "appointment/{id}"
    // ...
}
```

---

## How to Add a New Feature

Follow these steps in order. Do not skip steps.

**1. Define the domain model**
Create a pure Kotlin data class in `domain/model/`. No platform or framework imports.

**2. Define the repository interface**
Create an interface in `domain/repository/`. Methods return `Flow<T>` or `Result<T>`.

**3. Implement the repository**
Create `RepositoryImpl` in `data/repository/`. Wire `RemoteDataSource` (Supabase) and `LocalDataSource` (SQLDelight).

**4. Write use case(s)**
One class per use case in `domain/usecase/`. Single `invoke` operator. No state — stateless.

**5. Write unit tests**
Test use cases and repository implementations before writing any UI. Use Kotlin Test + MockK + Turbine.

**6. Define MVI contracts**
Create `State`, `Intent`, `Action`, `Effect` for the screen in `presentation/`.

**7. Implement the ViewModel**
Extend `BaseViewModel`. Map intents to actions. Call use cases. Emit state and effects.

**8. Generate screen in Stitch**
Use Google Stitch with NouriTheme tokens to generate the Composable. Apply M3 Botanical Garden theme.

**9. Integrate the Composable**
Wire the Stitch-generated Composable to the ViewModel. Collect `uiState` and `uiEffect`.

**10. Add route to navigation graph**
Register the screen in the Decompose navigation graph and add its route to `Routes`.

**11. Wire deep link (if needed)**
Add deep link handler in `AndroidManifest.xml` (Android) and `Info.plist` (iOS). Register route in `Routes`.

---

## Supabase Conventions

- Table names: `snake_case` (e.g. `compliance_logs`, `meal_plan_slots`)
- Every table has: `id uuid`, `created_at timestamptz`, `updated_at timestamptz`
- RLS enabled on every table — no exceptions
- All schema changes via `supabase migration new` — never edit prod schema directly via dashboard
- Timestamps stored in UTC
- All units metric (kg, cm)

---

## Koin Dependency Injection

Modules are defined in `commonMain` and initialized per platform:

```
commonMain/di/
    ├── DataModule.kt        # DataSources, Repositories
    ├── DomainModule.kt      # UseCases
    └── PresentationModule.kt # ViewModels

androidMain → KoinApplication in Application.onCreate()
iosMain     → KoinApplication in Swift entry point
wasmJsMain  → KoinApplication in main()
```

---

## Design System

- **Theme:** NouriTheme — M3 Botanical Garden palette
- **Light mode:** Fern Green `#4a7c59`, Marigold `#f9a620`, Terracotta `#b7472a`, Cream `#f5f3ed`
- **Dark mode:** M3 tonal variants
- **Typography:** Google Sans style
- All shared Composables live in `commonMain/presentation/shared/`
- Never use hardcoded colors or dimensions — always reference theme tokens

---

## Strict Code Conventions

These rules are enforced on every PR and every Claude Code session. No exceptions.

### MVI Contract Files

Each MVI contract MUST live in its own dedicated file. Never combine them.

```
presentation/feature/example/
    ├── ExampleState.kt     # data class only
    ├── ExampleIntent.kt    # sealed interface only
    ├── ExampleAction.kt    # sealed interface only
    ├── ExampleEffect.kt    # sealed interface only
    └── ExampleViewModel.kt   # ViewModel only
```

Combining any two of these into a single file is a violation.

### ViewModel Rules

- ViewModels live in `commonMain` — shared across Android, iOS, and Web
- Package: `com.nouri.screens.<feature>`
- ViewModels inject **UseCases only** — never Repositories directly
- Never use `viewModelScope.launch` directly — always use `suspend` functions and let `BaseViewModel` handle coroutine launching
- One ViewModel per screen

### UI Componentization

Complex screens MUST be broken down into individual Composable functions in separate files. No monolithic UI files.

```
presentation/feature/example/
    ├── ExampleScreen.kt          # top-level screen — wires ViewModel + sections
    ├── ExampleHeaderSection.kt   # header section composable
    ├── ExampleListSection.kt     # list section composable
    └── ExampleEmptyState.kt      # empty state composable
```

Every generated Composable MUST have an accompanying `@Preview` function in the same file.

```kotlin
@Composable
fun ExampleHeaderSection(title: String) {
    // ...
}

@Preview
@Composable
fun ExampleHeaderSectionPreview() {
    NouriTheme {
        ExampleHeaderSection(title = "Preview Title")
    }
}
```

### Imports — No Fully Qualified Names

NEVER use fully-qualified absolute package paths inline in code.

```kotlin
// ❌ Wrong
val status = com.nouri.domain.model.ComplianceStatus.FOLLOWED

// ✅ Correct
import com.nouri.domain.model.ComplianceStatus
val status = ComplianceStatus.FOLLOWED
```

Always import the class at the top of the file and use the short name.

### DI Registration

- ViewModels registered in `androidApp/di/AppModule.kt` (Android) and equivalent per platform
- UseCases registered in `shared/di/SharedModule.kt`
- Repositories and DataSources registered in `shared/di/DataModule.kt`

### Linting — Mandatory Before Completion

Every time code is generated, run and fix before marking done:

```bash
./gradlew ktlintCheck
./gradlew detektAll
```

Fix all violations before showing output. No suppression without an explanatory comment.
