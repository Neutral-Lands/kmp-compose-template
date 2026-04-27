# Nouri — Diet Companion

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
│   │   ├── datasource/    # Supabase + SQLDelight clients
│   │   ├── repository/    # RepositoryImpl
│   │   └── model/         # DTOs and local DB models
│   ├── domain/
│   │   ├── model/         # Domain models
│   │   ├── repository/    # Repository interfaces
│   │   └── usecase/       # Business logic
│   └── presentation/
│       └── viewmodel/     # MVI ViewModels
├── mobileMain/            # Shared mobile-only code
├── androidMain/           # Android platform implementations
├── iosMain/               # iOS platform implementations
└── wasmJsMain/            # Web (WASM) platform implementations
```

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

**Android** — create `local.properties` in the project root (never commit this file):

```properties
SUPABASE_URL=your_supabase_project_url
SUPABASE_ANON_KEY=your_supabase_anon_key
```

**iOS** — create `iosApp/Config.xcconfig` (never commit this file):

```
SUPABASE_URL = your_supabase_project_url
SUPABASE_ANON_KEY = your_supabase_anon_key
```

**Web** — create `.env.dev` in the project root (never commit this file):

```
SUPABASE_URL=your_supabase_project_url
SUPABASE_ANON_KEY=your_supabase_anon_key
```

> Credentials are stored in 1Password under the **Nouri** vault. Ask a team member for access.

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

### Run unit tests

```bash
./gradlew :shared:testDebugUnitTest
```

### Run all tests across all modules

```bash
./gradlew test
```

### Generate Kover coverage report

```bash
./gradlew koverHtmlReport
```

Report is generated at `shared/build/reports/kover/html/index.html`.

Coverage thresholds are enforced in CI — builds fail below the configured minimum.

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

CI runs on every PR and push to `main`. Steps:

1. `ktlintCheck`
2. `detektAll`
3. Unit tests
4. Android build
5. iOS build
6. Web (WASM) build
7. `supabase db lint`

A failed step blocks merge.

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
