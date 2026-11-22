# Feature Guides

## App Blocker & Profiles
- Configure per-app daily/weekly limits, shared app groups, and priority-ordered profiles (`Study`, `College`, `Work`, `Sleep`, `Reading`, `Weekend`, `Custom`).
- Emergency-safe apps (Dialer, Contacts, Settings) can be marked safe anytime.

## Focus Timer
- Supports **Pomodoro**, **Countdown**, and **Stopwatch** modes across 6 session types (`Study`, `Work`, `Reading`, `Coding`, `Creative`, `Custom`).
- Timer state persists via target epoch timestamps in DataStore + `FocusTimerService` + `BootCompletedReceiver`.

## Website Blocking (Local VPN)
- Starts `FocusNestVpnService` only after explicit Android `VpnService.prepare()` consent.
- Intercepts DNS queries on-device (`10.88.0.1`) against your blocklist/allowlist and offline domain packs. Zero external VPN servers.

## NFC, QR & Barcode Setup
- **NFC**: Tap any NDEF/UID tag under **More > NFC, QR & Barcode Vault** to bind it to Focus Start, Profile Unlock, or Quick Shield.
- **QR & Barcodes**: Generate personal FocusNest QR codes offline or scan existing QR/barcodes with CameraX + ZXing.

## Backup & Restore
- Export AES-256-GCM passphrase-encrypted backups or CSV/JSON/Printable HTML reports anytime.
