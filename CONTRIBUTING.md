# Contributing to Nouri

This document is the reference for all code standards and conventions. Every developer and Claude Code session must follow these rules. No exceptions.

---

## Code Style

### Tools

- **ktlint** — Kotlin formatting
- **Detekt** — code quality and complexity

```bash
./gradlew ktlintFormat   # auto-fix formatting issues
./gradlew ktlintCheck    # check only — no fixes
./gradlew detektAll      # run detekt across all modules
```

Both tools run in CI on every PR. A failed check blocks merge.

### Zero suppression policy

`@Suppress` annotations are never allowed without an explanatory comment directly above them stating why the suppression is justified.

```kotlin
// ❌ Not allowed
@Suppress("MagicNumber")
val timeout = 30000

// ✅ Allowed only with explanation
// Suppressed: timeout is a well-known constant defined by the APNs spec (30s)
@Suppress("MagicNumber")
val apnsTimeoutMs = 30000
```

### Linting before completion

Every time code is generated or modified, run ktlint and detekt and fix all violations **before** marking the task done or opening a PR.

---

## Naming Conventions

| Element | Convention | Example |
|---|---|---|
| Files (classes) | `PascalCase` | `ComplianceRepositoryImpl.kt` |
| Files (functions/extensions) | `camelCase` | `dateExtensions.kt` |
| Packages | `lowercase.dot.separated` | `com.nouri.domain.usecase` |
| Shared Composables | `PascalCase`, prefixed `Nouri` | `NouriButton`, `NouriCard` |
| Screen Composables | `PascalCase`, suffixed `Screen` | `MealLogScreen` |
| ViewModels | `ScreenNameViewModel` | `MealLogViewModel` |
| Use cases | `VerbNounUseCase` | `GetDailyComplianceLogsUseCase` |
| Repository interfaces | `NounRepository` | `ComplianceRepository` |
| Repository implementations | `NounRepositoryImpl` | `ComplianceRepositoryImpl` |
| State | `ScreenNameState` | `MealLogState` |
| Intent | `ScreenNameIntent` | `MealLogIntent` |
| Action | `ScreenNameAction` | `MealLogAction` |
| Effect | `ScreenNameEffect` | `MealLogEffect` |

---

## MVI Contract Files

Each MVI contract lives in its own dedicated file. Never combine them.

```
presentation/feature/meallog/
    ├── contracts/
    │   ├── MealLogState.kt
    │   ├── MealLogIntent.kt
    │   ├── MealLogAction.kt
    │   └── MealLogEffect.kt
    └── MealLogViewModel.kt
```

---

## No Hardcoded Values

| Type | Rule |
|---|---|
| Colors | Always use `NouriTheme.colors` — never `Color(0xFF...)` |
| Dimensions | Always use `NouriTheme.dimensions` — never `16.dp` inline |
| Strings | Always use string resources — never inline string literals in Composables |
| API keys | Always via build config — never hardcoded |

---

## UI Rules

### Componentization

Complex screens must be split into individual Composable functions in separate files. No monolithic UI files.

```
presentation/feature/meallog/
    ├── sections/
    │   ├── MealLogHeaderSection.kt
    │   ├── MealLogSlotListSection.kt
    │   └── MealLogEmptyState.kt
    └── MealLogScreen.kt          # top-level screen only
```

### Previews

Every Composable function must have an accompanying `@Preview` in the same file.

```kotlin
@Composable
fun MealLogHeaderSection(date: String) {
    // ...
}

@Preview
@Composable
fun MealLogHeaderSectionPreview() {
    NouriTheme {
        MealLogHeaderSection(date = "Monday, April 27")
    }
}
```

### Imports

Never use fully-qualified names inline. Always import at the top of the file.

```kotlin
// ❌ Wrong
val status = com.nouri.domain.model.ComplianceStatus.FOLLOWED

// ✅ Correct
import com.nouri.domain.model.ComplianceStatus
val status = ComplianceStatus.FOLLOWED
```

---

## ViewModel Rules

- ViewModels live in `commonMain` — shared across Android, iOS, and Web
- ViewModels inject **UseCases only** — never Repositories directly
- Never use `viewModelScope.launch` directly — use `suspend` functions and let `BaseViewModel` handle coroutine launching
- One ViewModel per screen

---

## Unit Tests

Unit tests are required for all use cases, repositories, ViewModels, and calculators. No exceptions.

### Tooling

- **Kotlin Test** — test framework (KMP-compatible)
- **MockK** — mocking
- **Turbine** — testing Kotlin Flows

### Naming

| Element | Convention |
|---|---|
| Test files | `ClassNameTest.kt` |
| Test functions | `given X when Y then Z` (backtick-enclosed, plain English) |

### Example

```kotlin
class GetDailyComplianceLogsUseCaseTest {

    @Test
    fun `given patient has logs when fetching today then returns logs`() {
        // ...
    }

    @Test
    fun `given no logs exist when fetching today then returns empty list`() {
        // ...
    }
}
```

### Coverage

Kover tracks coverage. Thresholds are enforced in CI — builds fail below the configured minimum.

```bash
./gradlew koverHtmlReport
# Report: shared/build/reports/kover/html/index.html
```

---

## Commit Messages

Format: `type: short imperative description [NEU-XX]`

| Type | When to use |
|---|---|
| `feat` | New feature |
| `fix` | Bug fix |
| `chore` | Config, tooling, dependencies |
| `docs` | Documentation only |
| `refactor` | Code change with no behavior change |
| `test` | Adding or fixing tests |

Examples:

```
feat: implement compliance log repository and use cases [NEU-39]
fix: correct slot index off-by-one in meal plan display [NEU-44]
chore: configure Supabase Kotlin SDK in shared module [NEU-15]
docs: add ARCHITECTURE.md system design guide [NEU-93]
```

---

## PR Process

1. Branch from `main`: `git checkout -b feature/NEU-XX-short-description`
2. Make changes — one ticket per branch
3. Run linting locally before pushing:
   ```bash
   ./gradlew ktlintFormat
   ./gradlew detektAll
   ```
4. Push and open PR via GitHub CLI:
   ```bash
   git push -u origin feature/NEU-XX-short-description
   gh pr create --base main
   ```
5. PR title must match commit format
6. Minimum 1 approval required
7. CI must pass — lint, tests, build, `supabase db lint`
8. **Squash merge only** — no merge commits

### PR checklist

Before requesting review, confirm:

- [ ] ktlint passes (`./gradlew ktlintCheck`)
- [ ] Detekt passes (`./gradlew detektAll`)
- [ ] Unit tests written and passing
- [ ] No hardcoded colors, dimensions, or strings
- [ ] No fully-qualified names inline
- [ ] MVI contracts each in their own file
- [ ] Every new Composable has a `@Preview`
- [ ] No credentials committed
- [ ] `supabase db lint` passes (if schema changed)
- [ ] README or ARCHITECTURE.md updated (if setup or architecture changed)
- [ ] Linear ticket linked in PR description (`Closes NEU-XX`)
