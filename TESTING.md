# Testing Guide (`TESTING.md`)

FocusNest uses pure JVM unit tests + Robolectric + Jetpack Compose UI tests:
- `FocusNestDomainUnitTest`: `UnlockChallengeEngine` (12 challenges including `BREATHING`, adaptive division math, 80%/90%/100% thresholds), `RulePriorityResolver` (grace periods & category daily caps), 6 `AccentPalette`s, local DNS packet parser/NXDOMAIN builder, AES-256-GCM backup crypto, and duration formatters.
- `FocusNestDatabaseAndRepoTest`: Room in-memory database CRUD, `MIGRATION_1_2` SQL schema upgrade verification, `HabitRepository` streaks & 7-day completion rates, signed/one-time/expiring QR token replay & expiry rejection, `BlockerRepository` group/category/daily limits, duplicate-guarded warnings & temporary unlocks, `FocusRepository` multi-day streaks, timer epoch persistence, and encrypted backup/restore round-trips.
- `ExampleRobolectricTest`: Compose UI onboarding-to-dashboard flow, 5-tab navigation + Habits seeding & verification, Hindi (`values-hi`) localization resolution, missing `UsageStatsManager` graceful fallback, missing NFC hardware, VPN consent check, and `BootCompletedReceiver`.

Run with:
```bash
gradle :app:testDebugUnitTest
```
