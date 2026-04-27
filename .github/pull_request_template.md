## Summary

<!-- What does this PR do? Why? -->

Closes <!-- NEU-XX -->

---

## Changes

<!-- List the key changes made -->

-
-
-

---

## Type of change

- [ ] `feat` — new feature
- [ ] `fix` — bug fix
- [ ] `chore` — config, tooling, dependencies
- [ ] `docs` — documentation only
- [ ] `refactor` — code change with no behavior change
- [ ] `test` — adding or fixing tests

---

## Testing

<!-- How was this tested? -->

- [ ] Unit tests written and passing
- [ ] Manually tested on Android
- [ ] Manually tested on iOS
- [ ] Manually tested on Web

---

## Checklist

- [ ] `./gradlew ktlintCheck` passes
- [ ] `./gradlew detektAll` passes
- [ ] Unit tests written and passing
- [ ] No hardcoded colors, dimensions, or strings
- [ ] No fully-qualified names inline
- [ ] MVI contracts each in their own file under `contracts/`
- [ ] Section Composables each in their own file under `sections/`
- [ ] Every new Composable has a `@Preview`
- [ ] No credentials committed
- [ ] `supabase db lint` passes (if schema changed)
- [ ] New migration created via `supabase migration new` (if schema changed)
- [ ] `ARCHITECTURE.md` updated (if architecture changed)
- [ ] `README.md` updated (if setup steps changed)
- [ ] Linear ticket linked above (`Closes NEU-XX`)
