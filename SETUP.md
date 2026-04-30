# New Project Setup Checklist

Use this guide after creating a repo from the `kmp-compose-template` template.

---

## 1 — Rename the package

Replace all occurrences of `com.neutrallands.nouri` with your package name and `Nouri` with your app name across the entire project.

```bash
# Replace package name in all Kotlin sources
find shared/src androidApp/src -name "*.kt" -exec \
  sed -i '' \
    's/com\.neutrallands\.nouri/com.your.package/g' {} +

# Move directory trees
for src_root in \
  shared/src/commonMain/kotlin \
  shared/src/commonTest/kotlin \
  shared/src/androidMain/kotlin \
  shared/src/iosMain/kotlin \
  shared/src/jvmMain/kotlin \
  shared/src/wasmJsMain/kotlin \
  androidApp/src/main/kotlin; do
  old="$src_root/com/neutrallands/nouri"
  new="$src_root/com/your/package"
  [ -d "$old" ] && mkdir -p "$(dirname $new)" && mv "$old" "$new"
done

# Update build files
sed -i '' \
  's/com\.neutrallands\.nouri/com.your.package/g' \
  shared/build.gradle.kts androidApp/build.gradle.kts build.gradle.kts

# Update Kover exclusions
sed -i '' \
  's/com\.neutrallands\.nouri/com.your.package/g' \
  shared/build.gradle.kts build.gradle.kts
```

---

## 2 — Update app identity

| File | Field | Change to |
|---|---|---|
| `androidApp/build.gradle.kts` | `applicationId` | `com.your.package` |
| `androidApp/build.gradle.kts` | `namespace` | `com.your.package.android` |
| `shared/build.gradle.kts` | `namespace` | `com.your.package.shared` |
| `shared/build.gradle.kts` | `packageName.set(...)` | `com.your.package.data.local` |
| `shared/build.gradle.kts` | `summary`, `homepage` | your app name / URL |
| `androidApp/src/main/res/values/strings.xml` | `app_name` | your app name |
| `iosApp/project.yml` | `PRODUCT_BUNDLE_IDENTIFIER` | your bundle ID |

---

## 3 — Set up Supabase

1. Create a new Supabase project at [supabase.com](https://supabase.com)
2. Note the **Project URL** and **Anon Key** from Project Settings → API
3. Link the CLI:
   ```bash
   supabase login
   supabase link --project-ref <your-project-ref>
   ```
4. Apply migrations:
   ```bash
   supabase db push
   ```
5. Update credentials in:
   - `local.properties` (Android) — `SUPABASE_URL` and `SUPABASE_ANON_KEY`
   - `iosApp/Configuration/Config.xcconfig` (iOS)
   - `.env.local` (Web) — copy from `.env.example`

---

## 4 — Set up Firebase

1. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Enable **Analytics** and **Crashlytics**
3. Register **Android app** with your `applicationId` → download `google-services.json` → place at `androidApp/google-services.json`
4. Register **iOS app** with your bundle ID → download `GoogleService-Info.plist` → place at `iosApp/iosApp/GoogleService-Info.plist`
5. Neither file should ever be committed (both are gitignored)

---

## 5 — Set up Cloudflare Pages (web hosting)

1. Create a [Cloudflare account](https://dash.cloudflare.com)
2. Create a Pages project: `npx wrangler pages project create <your-app-name>`
3. Update `projectName` in `.github/workflows/deploy-web.yml` to your project name

---

## 6 — Configure GitHub secrets

Go to **GitHub → Settings → Secrets → Actions** and add:

| Secret | Value |
|---|---|
| `SUPABASE_URL` | Your Supabase project URL |
| `SUPABASE_ANON_KEY` | Your Supabase anon key |
| `CLOUDFLARE_API_TOKEN` | Cloudflare API token (Edit Cloudflare Workers template) |
| `CLOUDFLARE_ACCOUNT_ID` | Cloudflare account ID (dashboard sidebar) |

---

## 7 — Configure Dependabot (optional)

Update `.github/dependabot.yml` — the assignee is currently set to `kaldarel`:
```yaml
assignees:
  - your-github-username
```

---

## 8 — Remove Nouri-specific migrations

The `supabase/migrations/` directory contains Nouri's schema. Replace with your own:

```bash
rm supabase/migrations/*.sql
rm supabase/seed.sql
supabase migration new initial_schema
# write your schema in the generated file
supabase db push
```

---

## 9 — Run the pre-flight checklist

```bash
./gradlew ktlintCheck detektAll        # lint
./gradlew :shared:jvmTest              # tests
./gradlew :shared:koverVerify          # coverage ≥ 70%
./gradlew :androidApp:assembleDebug    # Android build (requires google-services.json)
```

iOS: open `iosApp/iosApp.xcodeproj` in Xcode → build.

Web:
```bash
source .env.local && ./gradlew :shared:wasmJsBrowserDevelopmentRun
```

---

## What's included in this template

| Feature | Status |
|---|---|
| KMP scaffold — Android + iOS + Web (Wasm) | ✅ |
| Compose Multiplatform UI | ✅ |
| MVI architecture — BaseViewModel, State/Intent/Action/Effect | ✅ |
| Koin dependency injection | ✅ |
| Supabase SDK (Auth, Postgrest, Storage, Realtime) | ✅ |
| SQLDelight offline cache | ✅ |
| Firebase Analytics + Crashlytics (Android; iOS stub) | ✅ |
| Global error handling — DomainError + offline banner | ✅ |
| NouriTheme — Material3 light/dark with iOS dark mode | ✅ |
| Web sidebar layout scaffold | ✅ |
| Web env vars via webpack DefinePlugin | ✅ |
| CI pipeline — lint + tests + coverage PR comment | ✅ |
| Cloudflare Pages deploy workflow (manual) | ✅ |
| Dependabot for dependency updates | ✅ |
| ktlint + detekt — 70% coverage threshold | ✅ |
| Supabase preview branches per PR | ✅ |
