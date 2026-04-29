# Nouri — Diet Companion

[![CI](https://github.com/Neutral-Lands/Nouri/actions/workflows/ci.yml/badge.svg)](https://github.com/Neutral-Lands/Nouri/actions/workflows/ci.yml)

A cross-platform nutrition tracking app connecting nutritionists with their clients.

Built with Kotlin Multiplatform (KMP) + Compose Multiplatform, targeting Android, iOS, and Web.

---

## Roles

| Role | Capabilities |
|---|---|
| **Nutritionist** | Upload meal plans, manage patients, record appointments, enter body measurements |
| **Patient** | Log meal compliance, water intake, exercise, view progress, view appointments |

The patient app is locked until a nutritionist assigns an active plan.

---

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Compose Multiplatform (CMP) |
| Shared logic | Kotlin Multiplatform (KMP) |
| Backend | Supabase (Postgres, Auth, Storage, Realtime) |
| Dependency injection | Koin |
| Navigation | Decompose 3.1.0 |
| Networking | Ktor Client + kotlinx.serialization |
| Local cache | SQLDelight |
| Architecture | MVI |

---

## Architecture Overview

### Module structure

```
shared/
├── commonMain/
│   ├── data/
│   │   ├── datasource/
│   │   │   ├── remote/    # Supabase data sources
│   │   │   └── local/     # SQLDelight data sources
│   │   ├── repository/    # RepositoryImpl
│   │   └── model/         # DTOs and local DB models
│   ├── domain/
│   │   ├── model/         # Pure Kotlin domain models
│   │   ├── repository/    # Repository interfaces (no framework deps)
│   │   └── usecase/       # One class per use case
│   └── presentation/
│       ├── base/          # MVI base contracts (State, Intent, Action, Effect) + BaseViewModel
│       └── shared/        # NouriTheme, shared Composables, design tokens
├── mobileMain/            # Shared Android + iOS code (excludes Web)
├── androidMain/           # Android platform implementations
├── iosMain/               # iOS platform implementations
└── wasmJsMain/            # Web (WASM) platform implementations
```

**Dependency rules — never break these:**

| Layer | Rule |
|---|---|
| `domain` | Zero platform or framework imports. Pure Kotlin only. |
| `presentation` | Depends on `domain` only. Never imports from `data`. |
| `data` | Implements `domain` interfaces. |
| Platform modules | Depend on `commonMain` only. |

Enforcement: Detekt (NEU-21) and architecture tests (NEU-82). See [ARCHITECTURE.md](ARCHITECTURE.md) for full conventions.

### MVI flow

```
User Action
    │
    ▼
ViewModel (Intent)
    │
    ▼
UseCase (domain logic)
    │
    ▼
Repository (interface)
    │
    ├── RemoteDataSource (Supabase)
    └── LocalDataSource  (SQLDelight)
    │
    ▼
ViewModel (State + Effect)
    │
    ▼
Composable UI
```

Data flow is strictly one-directional: `presentation` → `domain` → `data`. No reverse dependencies. `domain` has zero platform dependencies.

---

## Project Structure

```
Nouri/
├── androidApp/          # Android entry point
├── iosApp/              # iOS entry point (Swift/Xcode)
├── shared/
│   ├── commonMain/      # Shared business logic, domain, data
│   ├── mobileMain/      # Shared mobile-only code
│   ├── androidMain/     # Android platform implementations
│   ├── iosMain/         # iOS platform implementations
│   └── wasmJsMain/      # Web (WASM) platform implementations
├── supabase/
│   ├── migrations/      # All schema migrations (versioned)
│   └── functions/       # Edge Functions
└── gradle/
    └── libs.versions.toml
```

---

## Prerequisites

- **Android Studio** (Hedgehog or newer) with KMP plugin
- **Xcode 15+** (for iOS builds)
- **JDK 17+**
- **Node.js 18+** (for Web WASM build tooling)
- **Supabase CLI** — `brew install supabase/tap/supabase`
- **Docker Desktop** — required for local Supabase (`supabase start`)

---

## Getting Started

### 1. Clone the repo

```bash
git clone https://github.com/your-org/nouri.git
cd nouri
```

### 2. Set up credentials

Credentials are stored in **1Password → Nouri vault**. Ask a team member for access. Never commit credential files.

**Android** — copy the example and fill in values from 1Password:

```bash
cp local.properties.example local.properties
# edit local.properties with your values
```

```properties
sdk.dir=/Users/yourname/Library/Android/sdk
SUPABASE_URL=https://zpkhhpkpqfqodezguftf.supabase.co   # prod
SUPABASE_ANON_KEY=<anon key from 1Password>
```

**iOS** — copy the example and fill in values, then re-run xcodegen:

```bash
cp iosApp/Configuration/Config.xcconfig.example iosApp/Configuration/Config.xcconfig
# edit Config.xcconfig with your values
cd iosApp && xcodegen generate --spec project.yml && cd ..
./gradlew :shared:podInstall
```

```
SUPABASE_URL = https://zpkhhpkpqfqodezguftf.supabase.co
SUPABASE_ANON_KEY = <anon key from 1Password>
```

**Web** — pending NEU-84 (env var strategy for Wasm).

**Environments:**

| Environment | Supabase project | URL |
|---|---|---|
| prod | `nouri-prod` | `https://zpkhhpkpqfqodezguftf.supabase.co` |
| dev | `nouri-dev` *(create in Supabase dashboard — separate project for dev/staging)* | — |

### 3. Link Supabase CLI to production project

```bash
supabase login
supabase link --project-ref zpkhhpkpqfqodezguftf
```

### 4. Run the app

**Android:**
```bash
./gradlew :androidApp:installDebug
```

**iOS:**

Open `iosApp/iosApp.xcodeproj` in Xcode and press Run.

**Web:**
```bash
./gradlew :shared:wasmJsBrowserDevelopmentRun
```

---

## Local Development with Supabase

Local development runs a full Supabase stack via Docker.

### Start local Supabase

```bash
supabase start
```

This spins up a local Postgres instance at `http://127.0.0.1:54321`. Local Studio is available at `http://127.0.0.1:54323`.

### Stop local Supabase

```bash
supabase stop
```

---

## Database Migrations

All schema changes must be versioned via migrations. **Never apply ad-hoc SQL to production.**

### Create a new migration

```bash
supabase migration new <descriptive_name>
# Example: supabase migration new add_hunger_logs_table
```

Write your SQL in the generated file under `supabase/migrations/`.

### Apply migrations to production

```bash
supabase start        # Docker must be running first
supabase db push
```

### Check local/remote sync

```bash
supabase db diff      # requires Docker running
```

Should return empty if local and remote are in sync.

### Lint migrations (also runs in CI)

```bash
supabase db lint
```

---

## Seed Data

The `subscription_plans` table must be seeded before any nutritionist can complete sign-up.

```bash
supabase db reset     # applies migrations + seed locally
```

Or apply manually via the Supabase SQL editor:

```sql
insert into subscription_plans (id, name, max_patients, price_usd_cents) values
  ('free',    'Free',    3,  0),
  ('starter', 'Starter', 10, 999),
  ('pro',     'Pro',     -1, 2999);
```

---

## Testing

### Tooling

| Tool | Purpose |
|---|---|
| **Kotlin Test** | KMP-compatible test framework — works on Android, iOS, Web |
| **Turbine** | Flow testing — `flow.test { awaitItem() }` |
| **Kotlinx Coroutines Test** | `runTest`, `TestCoroutineDispatcher` |
| **MockK** | Mocking — JVM/Android/Native only; use hand-written fakes in `commonTest` |
| **Kover** | JVM code coverage — enforced in CI |

### What to test

- All use cases — every public function, happy path and error cases
- All repository implementations — local reads/writes, sync flag logic
- All domain calculators — pure functions, boundary values
- All `BaseViewModel` state transitions — intent → action → state update
- Sync logic — offline queue, conflict resolution

### What NOT to test

- Composable UI functions — no UI tests for MVP
- Data classes and mappers with no logic
- Supabase client calls — manually integration-tested

### Run tests (Wasm target)

```bash
./gradlew :shared:wasmJsBrowserTest
```

### Generate Kover coverage report

```bash
./gradlew koverHtmlReport
```

Report at `build/reports/kover/html/index.html`.

### Coverage thresholds (enforced in CI — NEU-18)

| Layer | Minimum |
|---|---|
| `com.nouri.domain` | 80% line coverage |
| `com.nouri.data` | 70% line coverage |
| `com.nouri.presentation` | 70% line coverage |

```bash
./gradlew :shared:koverVerify   # fails if below thresholds
```

> **MockK note:** `io.mockk:mockk` supports JVM, Android, and Kotlin/Native only — not Wasm. Use hand-written fakes in `commonTest`. MockK is available for JVM-specific test source sets once CI (NEU-18) configures platform runners.

---

## Code Style

- **ktlint** enforces Kotlin formatting
- **Detekt** enforces code quality rules
- Zero suppression policy — no `@Suppress` without an explanatory comment

```bash
./gradlew ktlintFormat   # auto-fix formatting
./gradlew ktlintCheck    # check only
./gradlew detektAll      # run detekt on all modules
```

---

## Branch Strategy

| Branch | Purpose |
|---|---|
| `main` | Always deployable. Protected — no direct commits. |
| `feature/NEU-XX-short-description` | One branch per Linear ticket, branched from `main`. |

All changes go through pull requests. PRs require:
- 1 approval minimum
- CI passing (lint + tests + build)
- Supabase preview branch validated (auto-provisioned per PR)

Preview branches are automatically created by the Supabase GitHub integration and torn down on merge.

---

## CI Pipeline

CI runs on every PR and push to `main` via GitHub Actions (`.github/workflows/ci.yml`).

| Job | What runs |
|---|---|
| **Lint** | `./gradlew ktlintCheck detektAll` |
| **Test** | `./gradlew :shared:jvmTest koverHtmlReport` — uploads coverage as artifact |

Both jobs must pass before merge. Android, iOS, and Web builds are verified locally.

### Branch protection (GitHub Settings → Branches → `main`)

- Required status checks: `Lint`, `Unit Tests + Coverage`
- Require branches to be up to date before merging
- No direct commits to `main`

---

## iOS Swift/Kotlin Bridge

This section explains how the Kotlin Multiplatform shared module integrates with the Swift iOS host.

### Architecture overview

```
iOSApp.swift          ← SwiftUI @main entry point, bootstraps Koin
    └── ContentView.swift     ← SwiftUI View, hosts the Compose layer
            └── ComposeView   ← UIViewControllerRepresentable wrapper
                    └── MainViewController.kt  ← exposes Compose UI to Swift
                            └── App()          ← shared Composable tree
```

### `MainViewController.kt` — Kotlin side

`shared/src/iosMain/kotlin/com/nouri/MainViewController.kt` calls `ComposeUIViewController { }` to create a standard `UIViewController` that hosts the Compose Multiplatform tree:

```kotlin
fun MainViewController() = ComposeUIViewController {
    val connectivityObserver = KoinPlatform.getKoin().get<ConnectivityObserver>()
    App(connectivityObserver = connectivityObserver)
}
```

The Kotlin Gradle plugin exposes this function to Swift as `MainViewControllerKt.MainViewController()` (via the `shared` CocoaPods framework compiled by `./gradlew :shared:podInstall`).

### `ContentView.swift` — Swift side

`iosApp/iosApp/ContentView.swift` wraps the Kotlin `UIViewController` using `UIViewControllerRepresentable`:

```swift
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView().ignoresSafeArea(.all)
    }
}
```

`.ignoresSafeArea(.all)` lets the Compose `Scaffold` manage insets itself.

### `iOSApp.swift` — Koin bootstrap

`iosApp/iosApp/iOSApp.swift` is the SwiftUI `@main` entry point. It reads credentials from `Bundle.main` and calls `initKoin` before the first frame renders:

```swift
@main
struct iOSApp: App {
    init() {
        let url = Bundle.main.infoDictionary?["SUPABASE_URL"] as? String ?? ""
        let key = Bundle.main.infoDictionary?["SUPABASE_ANON_KEY"] as? String ?? ""
        KoinInitializerKt.initKoin(supabaseUrl: url, supabaseAnonKey: key)
    }
    var body: some Scene {
        WindowGroup { ContentView() }
    }
}
```

### Credentials flow

```
Config.xcconfig          (gitignored — copied from Config.xcconfig.example)
    │  SUPABASE_URL = https://...
    │  SUPABASE_ANON_KEY = <key>
    ▼
Info.plist               (reads from xcconfig via $(SUPABASE_URL) variable expansion)
    ▼
Bundle.main.infoDictionary   (read at runtime in iOSApp.swift init)
    ▼
KoinInitializerKt.initKoin(supabaseUrl:supabaseAnonKey:)
```

Never commit `Config.xcconfig` — credentials live in **1Password → Nouri vault**.

### Dark mode

`NouriTheme` uses `isSystemInDarkTheme()` (Compose Multiplatform), which on iOS reads `UIUserInterfaceStyle` automatically. No additional Swift-side setup is required — toggling Appearance in iOS Settings → Developer triggers the correct color scheme.

---

## Push Notifications Setup

Push notifications use FCM (Android) and APNs (iOS) via a Supabase Edge Function.

### Android — Firebase Cloud Messaging (FCM)

1. Open [Firebase Console](https://console.firebase.google.com) → select the **Nouri** project
2. Download `google-services.json` → place in `androidApp/`
3. Never commit this file — it is in `.gitignore`

### iOS — Apple Push Notification service (APNs)

1. Open [Apple Developer Portal](https://developer.apple.com) → Certificates, Identifiers & Profiles
2. Generate an APNs key (`.p8` file) for the Nouri bundle ID
3. Upload the `.p8` key to Firebase Console → Project Settings → Cloud Messaging
4. Enable Push Notifications capability in Xcode → Signing & Capabilities

### Supabase Edge Function

The `send-push-notification` function under `supabase/functions/` handles delivery to both FCM and APNs. FCM server key must be set as a Supabase secret:

```bash
supabase secrets set FCM_SERVER_KEY=your_fcm_server_key
```

---

## Error Handling Strategy

| Scenario | Pattern |
|---|---|
| Transient (network timeout) | Snackbar via `BaseViewModel.handleError()` → `error` flow → global snackbar |
| Unrecoverable (auth expired) | `NouriErrorState` full-screen composable + retry button |
| Silent (background sync fail) | Log only, retry queued |

`BaseViewModel.handleError(throwable)` converts to a `DomainError` and emits on the `error` flow. The global `App` scaffold collects this and shows the appropriate snackbar or error state. Platform connectivity is observed via `ConnectivityObserver` — Android uses `ConnectivityManager`, iOS/Web are stubbed (NEU-136, NEU-84).

---

## Key Docs

| Document | Purpose |
|---|---|
| `ARCHITECTURE.md` | Module structure, MVI flow, data layer conventions |
| `CONTRIBUTING.md` | Code standards, naming conventions, PR checklist |
| `CLAUDE.md` | Project context and conventions for Claude Code sessions |

---

## External Links

| Resource | Link |
|---|---|
| Linear — Diet Companion project | https://linear.app/neutral-lands |
| Supabase dashboard — nouri-prod | https://supabase.com/dashboard/project/zpkhhpkpqfqodezguftf |
| Firebase Console | https://console.firebase.google.com |
| 1Password — Nouri vault | Ask a team member for access |
