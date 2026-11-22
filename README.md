# FocusNest

**Own Your Time. Protect Your Focus.**

FocusNest is a 100% free, privacy-first, open-source Android digital wellbeing, focus timer, screen-time analytics, and app-blocking application built with Kotlin, Jetpack Compose, Material 3, Room, DataStore, and WorkManager.

## Core Principles

- **100% Free & Open Source** — No paywalls, no premium tiers, no ads.
- **Offline-First & Zero Trackers** — No `INTERNET` permission, no telemetry, no mandatory accounts.
- **Transparent Permissions** — Every Android permission (`PACKAGE_USAGE_STATS`, `AccessibilityService`, `VpnService`, `NotificationListenerService`, `CAMERA`, `NFC`) is optional and requested contextually.
- **Owner-Controlled** — Clear emergency recovery, encrypted local AES-256-GCM backups, and zero stealth device locking.

## Architecture Overview

```mermaid
flowchart TD
    UI[Jetpack Compose M3 UI - Home / Focus / Blocker / Insights / More] --> VM[FocusNestViewModel]
    VM --> REPO[Repositories: UsageStats / Blocker / Focus / Insights & Backup]
    REPO --> ROOM[(Room SQLite - 18 Entities)]
    REPO --> DS[(DataStore Preferences & Timer Epoch State)]
    REPO --> SYS[Android System APIs: UsageStatsManager / PackageManager / NFC]
    SVC[Services: FocusTimerService / Accessibility / Local DNS VPN / NotificationListener] --> REPO
    WORK[WorkManager + BootCompletedReceiver + AppWidgets] --> REPO
```

## Key Features

- **Home Dashboard**: Real screen-time goal ring, remaining allowance, top apps + launch counts, live focus status, quick shield toggle, weekly chart, and daily intention.
- **Focus Timer**: Pomodoro, Countdown, and Stopwatch modes with 6 session types, custom break/cycle settings, reboot-persistent epoch timing, streaks, and timeline history.
- **App Blocker & Profiles**: Installed app search, categories, shared app groups, daily/weekly limits, 6+ preset profiles (`Study`, `College`, `Work`, `Sleep`, `Reading`, `Weekend`, `Custom`), and deterministic rule-priority resolution.
- **11 Unlock Challenges**: Strict Mode, Fixed Cooldown, Temporary Unlock, Offline QR Scan, Offline Barcode Scan, NFC Tag Tap, Adaptive Math, Sentence Typing, Personal Intention, Daily Unlock Cap, and Reflection Pause.
- **Local Website Filter**: Optional on-device `VpnService` DNS sinkhole (blocklist, allowlist, offline packs) with zero external servers.
- **Reels & Shorts Reduction**: Configurable pause prompts and daily limits for Instagram Reels, YouTube Shorts, Facebook Reels, and TikTok.
- **Insights & Exports**: Daily/weekly/monthly/custom-range analytics, category breakdowns, best focus period, CSV, JSON, and printable HTML reports.
- **Widgets & Hardware**: 3 Home Screen widgets, CameraX + ZXing offline QR/Barcode scanner & generator, native Android NFC tag vault, and BiometricPrompt support for Invincible Mode.

## Building & Testing

```bash
gradle :app:assembleDebug
gradle :app:testDebugUnitTest
```

See [BUILD.md](BUILD.md), [TESTING.md](TESTING.md), and [docs/getting-started.md](docs/getting-started.md) for full details.
