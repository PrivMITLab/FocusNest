# Testing Guide (`TESTING.md`)

FocusNest uses pure JVM unit tests + Robolectric + Jetpack Compose UI tests:
- `FocusNestDomainUnitTest`: `UnlockChallengeEngine`, `RulePriorityResolver`, local DNS packet parser/NXDOMAIN builder, AES-256-GCM backup crypto, and duration formatters.
- `FocusNestDatabaseAndRepoTest`: Room in-memory database CRUD, `BlockerRepository` group/daily limits & temporary unlocks, `FocusRepository` multi-day streaks, timer epoch persistence, and encrypted backup/restore round-trips.
- `ExampleRobolectricTest`: Compose UI onboarding-to-dashboard flow, 5-tab navigation, missing `UsageStatsManager` graceful fallback, missing NFC hardware, VPN consent check, and `BootCompletedReceiver`.

Run with:
```bash
gradle :app:testDebugUnitTest
```
