# Database Schema (`FocusNestDatabase`)

FocusNest uses Room SQLite (`focusnest_local.db`) with 18 local entities:

1. `installed_apps` (`InstalledAppEntity`) — Discovered launcher apps, inferred category, distracting flag, emergency whitelist flag.
2. `app_usage` (`AppUsageEntity`) — Composite PK `(dateKey, packageName)` storing measured `UsageStatsManager` foreground duration, launch count, and first/last timestamps.
3. `app_limits` (`AppLimitEntity`) — Per-app daily/weekly limits, manual block flag, unlock challenge policy, and `tempUnlockUntilEpochMs`.
4. `app_groups` (`AppGroupEntity`) — Custom groups with shared daily limits.
5. `block_profiles` (`BlockProfileEntity`) — `Study`, `College`, `Work`, `Sleep`, `Reading`, `Weekend`, `Custom` profiles with blocked/allowed packages, priority, and strict mode.
6. `blocking_schedules` (`BlockingScheduleEntity`) — Weekday & time-window rules linked to profiles (supports overnight wraparound).
7. `focus_sessions` (`FocusSessionEntity`) — Completed and interrupted Pomodoro/Countdown/Stopwatch sessions with notes.
8. `focus_goals` (`FocusGoalEntity`) — Daily screen-time goal, daily/weekly focus goals, daily sessions goal.
9. `unlock_rules` (`UnlockRuleEntity`) — Configurable challenge defaults.
10. `unlock_history` (`UnlockHistoryEntity`) — Audit trail of granted & denied unlocks + intention text.
11. `website_rules` (`WebsiteRuleEntity`) — Local DNS blocklist/allowlist domains and hit counts.
12. `reminders` (`ReminderEntity`) — Mindful break/bedtime/intention reminders.
13. `notification_records` (`NotificationRecordEntity`) — Local-only notification log when Notification Listener is enabled.
14. `daily_analytics` (`DailyAnalyticsEntity`) — Daily rollups for screen time, focus minutes, blocked attempts, unlocks, and Reels interventions.
15. `user_preferences` (`UserPreferenceEntity`) — Key-value Room backup store.
16. `nfc_tags` (`NfcTagEntity`) — Registered NFC tag UIDs and bound actions.
17. `qr_tokens` (`QrTokenEntity`) — Registered QR & Barcode tokens.
18. `short_form_rules` (`ShortFormRuleEntity`) — Per-app Reels/Shorts daily caps and pause durations.
