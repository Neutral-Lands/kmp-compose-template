# CLAUDE.md — Nouri Project Context

This file is read automatically by Claude Code on every session. Follow everything here exactly. Do not deviate from these conventions without explicit instruction.

---

## Project Overview

**App name:** Nouri — Diet Companion
**Platform:** Android, iOS, Web (Wasm)
**Purpose:** Cross-platform nutrition tracking app connecting nutritionists with their patients.

### Two roles

| Role | Responsibilities |
|---|---|
| **Nutritionist** | Upload PDF meal plans, define named meal slots per plan, enter anthropometric measurements, manage appointments, manage patient assignments |
| **Patient** | Read meal plan offline, log meal compliance per slot, log water intake, log exercise, view progress, view appointments |

The patient app is locked until a nutritionist assigns an active plan.

---

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Compose Multiplatform (CMP) |
| Shared logic | Kotlin Multiplatform (KMP) |
| Backend | Supabase (Auth, Postgres, Storage, Realtime) |
| DI | Koin |
| Navigation | Decompose 3.1.0 |
| Local cache | SQLDelight (offline compliance logs only) |
| Architecture | MVI with shared BaseViewModel |
| Analytics | Firebase Analytics + Crashlytics |
| Networking | Ktor Client + kotlinx.serialization |

---

## Module Structure

```
shared/
├── commonMain/
│   ├── data/
│   │   ├── datasource/
│   │   │   ├── remote/        # Supabase data sources
│   │   │   └── local/         # SQLDelight data sources
│   │   ├── repository/        # RepositoryImpl
│   │   └── model/             # DTOs + local DB models
│   ├── domain/
│   │   ├── repository/        # Repository interfaces only
│   │   ├── usecase/           # One class per use case
│   │   └── model/             # Pure Kotlin domain models
│   └── presentation/
│       ├── base/              # BaseViewModel, contracts base types
│       └── shared/            # NouriTheme, shared Composables
├── androidMain/
├── iosMain/
└── wasmJsMain/
```

### Dependency rules — enforced, never break these

- `domain` → zero platform or framework imports. Pure Kotlin only.
- `presentation` → depends on `domain` only. Never imports from `data`.
- `data` → implements `domain` interfaces.
- Platform modules → depend on `commonMain` only.

---

## MVI Conventions — Follow Exactly

### Contracts

Each contract lives in its own file under a `contracts/` subfolder. Never combine.

```
presentation/feature/example/
├── contracts/
│   ├── ExampleState.kt      # data class, default loading state
│   ├── ExampleIntent.kt     # sealed interface, user interactions only
│   ├── ExampleAction.kt     # sealed interface, internal ViewModel actions
│   └── ExampleEffect.kt     # sealed interface, one-shot events
└── ExampleViewModel.kt
```

### Contract definitions

```kotlin
// State — data class with defaults
data class ExampleState(
    val isLoading: Boolean = false,
    val items: List<Item> = emptyList(),
    val error: String? = null
)

// Intent — user interactions only
sealed interface ExampleIntent {
    data class Load(val id: String) : ExampleIntent
    data object Refresh : ExampleIntent
}

// Action — internal ViewModel actions
sealed interface ExampleAction {
    data class Fetch(val id: String) : ExampleAction
}

// Effect — one-shot events (navigation, toasts, dialogs)
sealed interface ExampleEffect {
    data class NavigateTo(val route: String) : ExampleEffect
    data class ShowSnackbar(val message: String) : ExampleEffect
}
```

### ViewModel rules

- Extend `BaseViewModel`
- Live in `commonMain` — shared across all platforms
- Inject **UseCases only** — never Repositories directly
- Never use `viewModelScope.launch` — use `suspend` functions, let `BaseViewModel` handle scope
- Use `updateState {}` reducer for immutable state updates
- `handleIntent()` maps intents to actions internally

```kotlin
class ExampleViewModel(
    private val getItemsUseCase: GetItemsUseCase
) : BaseViewModel<ExampleState, ExampleIntent, ExampleAction, ExampleEffect>(
    initialState = ExampleState()
) {
    override fun handleIntent(intent: ExampleIntent) {
        when (intent) {
            is ExampleIntent.Load -> handleAction(ExampleAction.Fetch(intent.id))
            ExampleIntent.Refresh -> handleAction(ExampleAction.Fetch(currentState.id))
        }
    }

    override suspend fun handleAction(action: ExampleAction) {
        when (action) {
            is ExampleAction.Fetch -> fetchItems(action.id)
        }
    }

    private suspend fun fetchItems(id: String) {
        updateState { copy(isLoading = true) }
        getItemsUseCase(id)
            .onSuccess { items -> updateState { copy(isLoading = false, items = items) } }
            .onFailure { error -> handleError(error) }
    }
}
```

---

## UI Conventions

### File structure

```
presentation/feature/meallog/
├── contracts/
│   ├── MealLogState.kt
│   ├── MealLogIntent.kt
│   ├── MealLogAction.kt
│   └── MealLogEffect.kt
├── sections/
│   ├── MealLogHeaderSection.kt
│   ├── MealLogSlotListSection.kt
│   └── MealLogEmptyState.kt
└── MealLogScreen.kt
```

### Componentization rule

Complex screens MUST be split into section Composables under `sections/`. No monolithic screen files.

### Preview rule

Every Composable MUST have an accompanying `@Preview` in the same file.

```kotlin
@Composable
fun MealLogHeaderSection(date: String) { ... }

@Preview
@Composable
fun MealLogHeaderSectionPreview() {
    NouriTheme {
        MealLogHeaderSection(date = "Monday, April 27")
    }
}
```

### Import rule

Never use fully-qualified names inline. Always import at the top of the file.

```kotlin
// ❌ Wrong
val status = com.nouri.domain.model.ComplianceStatus.FOLLOWED

// ✅ Correct
import com.nouri.domain.model.ComplianceStatus
val status = ComplianceStatus.FOLLOWED
```

---

## Naming Conventions

| Element | Convention | Example |
|---|---|---|
| ViewModels | `ScreenNameViewModel` | `MealLogViewModel` |
| Use cases | `VerbNounUseCase` | `GetDailyComplianceLogsUseCase` |
| Repository interfaces | `NounRepository` | `ComplianceRepository` |
| Repository implementations | `NounRepositoryImpl` | `ComplianceRepositoryImpl` |
| State | `ScreenNameState` | `MealLogState` |
| Intent | `ScreenNameIntent` | `MealLogIntent` |
| Action | `ScreenNameAction` | `MealLogAction` |
| Effect | `ScreenNameEffect` | `MealLogEffect` |
| Shared Composables | `Nouri` prefix | `NouriButton`, `NouriCard` |
| Screen Composables | `ScreenName` suffix | `MealLogScreen` |
| Test functions | backtick plain English | `` `given patient has logs when fetching today then returns logs` `` |

---

## NouriTheme — Always Use Tokens

Never hardcode colors, dimensions, or typography values.

```kotlin
// ❌ Wrong
Text(text = "Hello", color = Color(0xFF4a7c59), fontSize = 16.sp)

// ✅ Correct
Text(text = "Hello", color = NouriTheme.colors.primary, style = NouriTheme.typography.bodyLarge)
```

| Token type | Access |
|---|---|
| Colors | `NouriTheme.colors.*` |
| Typography | `NouriTheme.typography.*` |
| Dimensions | `NouriTheme.dimensions.*` |

---

## Supabase Conventions

- RLS enabled on every table — no exceptions
- Every table has: `id uuid`, `created_at timestamptz`, `updated_at timestamptz`
- All timestamps in UTC
- All units metric (kg, cm)
- Migrations via `supabase migration new` — never edit prod schema via SQL editor
- Remote calls only in `data` layer — never in `domain` or `presentation`
- Storage bucket: `meal-plans` — PDF only (`application/pdf`)

---

## Database Schema — Tables

| Table | Purpose |
|---|---|
| `profiles` | Users — nutritionists and patients |
| `subscription_plans` | Static plan tiers (Free, Starter, Pro) |
| `nutritionist_subscriptions` | Nutritionist active plan + cumulative patient count |
| `nutritionist_patients` | Assignment relationship + status lifecycle |
| `meal_plans` | PDF meal plan metadata + storage path |
| `meal_plan_slots` | Named meal slots per plan (defined by nutritionist) |
| `compliance_logs` | Daily meal compliance per slot per patient |
| `water_logs` | Binary daily water goal tracking |
| `exercise_logs` | Binary daily exercise tracking |
| `appointments` | Full appointment lifecycle |
| `anthropometric_measurements` | All body measurements per consultation |

---

## How to Add a New Feature

Follow in order. Do not skip steps.

1. Define domain model in `domain/model/`
2. Define repository interface in `domain/repository/`
3. Implement repository in `data/repository/` — wire remote + local data sources
4. Write use case(s) in `domain/usecase/` — one class, one `invoke` operator
5. Write unit tests — use case + repository — before any UI work
6. Create `contracts/` folder with `State`, `Intent`, `Action`, `Effect` — each in own file
7. Implement ViewModel extending `BaseViewModel`
8. Generate screen Composable in Stitch using NouriTheme tokens
9. Integrate Composable — collect `state`, dispatch `intents`, handle `effects`
10. Add route to `Routes` object
11. Wire deep link if needed (`AndroidManifest.xml` + `Info.plist`)

---

## What to Run Before Every Commit

```bash
./gradlew ktlintFormat
./gradlew ktlintCheck
./gradlew detektAll
./gradlew testDebugUnitTest
```

All must pass. Fix all violations before committing.

---

## Key Decisions — Do Not Reverse Without Discussion

| Decision | Reason |
|---|---|
| Supabase over Firebase | Relational model fits nutritionist ↔ patient ↔ plan structure |
| SQLDelight for local cache only | Supabase is source of truth — SQLDelight is offline buffer only |
| Compose Multiplatform Wasm | Not Kotlin/JS — wasmJsMain source set |
| MVI over MVVM | Explicit intent/action separation improves testability |
| Stitch for screen generation | Composables come from Stitch — dev work is integration only |
| Decompose for navigation | Lifecycle-aware, KMP-native, handles back stack on all platforms |
| Metric units only | App targets Peru — kg, cm throughout |
| UTC timestamps | All stored in UTC, displayed in local time |
