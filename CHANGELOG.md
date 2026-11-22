# Changelog

## [1.1.0] - 2026-11-22
- **Habits & Routines Tracker**: Offline Room-backed `HabitEntity` & `HabitCompletionEntity` with streaks, 7-day completion strips, daily/weekly completion bar charts, and starter habits.
- **Breathing Unlock Challenge (12th Method)**: Guided 4-2-4 inhale-hold-exhale orb challenge added to `UnlockChallengeType.BREATHING` alongside predefined intention chips and adaptive division math.
- **Category Limits, Grace Periods & Progressive Warnings**: `CategoryLimitEntity` daily caps, per-app grace windows (0–15 min), and 80%/90%/100% WorkManager threshold alerts.
- **Focus Controls & Monotonic Timer**: Quick duration presets, `+5m` Extend, `Restart`, notification `Pause`, Break screen `Skip`, and `SystemClock.elapsedRealtime()` clock-tamper resilience.
- **Signed One-Time & Expiring QR/Barcode Tokens**: Replay-protected SHA-256 verification and automatic expiry enforcement in NFC & QR Vault.
- **Per-App VPN Network Scope**: Selectively route chosen packages through `FocusNestVpnService`.
- **Android Quick Settings Tile & Localization**: `FocusQuickTileService` toggle, full Hindi (`values-hi`) localization + runtime language switcher, `INDIGO_FOCUS` 6th accent palette, and installed app icons across lists.
- **Granular Privacy Controls**: Separate cleanup actions for usage history, notification logs, and habits without wiping everything.
- **Room Database v2**: Real SQL `MIGRATION_1_2` preserving all v1 user records.

## [1.0.0] - 2026-11-21
- Initial release of FocusNest (`com.aistudio.focusnest.fnwbx`).
- 10-step contextual onboarding flow with skip-friendly optional permissions.
- Home Dashboard with animated screen-time goal ring, quick shield, daily intention, and weekly chart.
- Focus Sanctuary timer (Pomodoro, Countdown, Stopwatch) with foreground service & reboot recovery.
- Configurable App Blocker, 6+ preset profiles, schedules, shared app groups, and 11 unlock challenges.
- Local-only `VpnService` DNS website filter, Reels & Shorts reduction, Mindful Tools, and Invincible Mode.
- Insights & Reports with CSV, JSON, and printable HTML exports, plus AES-256-GCM encrypted backups.
- 3 Home Screen widgets, CameraX + ZXing offline QR/Barcode scanner/generator, and NFC Tag Vault.
