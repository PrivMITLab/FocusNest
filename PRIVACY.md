# Privacy Policy

FocusNest is designed so that your digital wellbeing data belongs exclusively to you.

1. **Zero Data Collection**: FocusNest does not collect, sell, rent, or transmit personal data, usage statistics, notification text, accessibility events, or browsing queries to any remote server.
2. **No Internet Permission**: The application manifest does not request `android.permission.INTERNET`.
3. **Local Storage Only**: All usage snapshots, focus sessions, block rules, NFC tag UIDs, QR tokens, and optional notification summaries are stored strictly in private app storage (Room SQLite & DataStore).
4. **Optional Sensitive Permissions**:
   - `PACKAGE_USAGE_STATS`: Used solely to compute on-device screen time and app launch counts.
   - `AccessibilityService`: Used solely to detect foreground app launches and configured short-form video views locally when enabled.
   - `VpnService`: Operates strictly as an on-device DNS sinkhole for domain filtering. Zero packets leave your device through FocusNest.
   - `NotificationListenerService`: Stores minimal notification summaries locally and mutes configured notifications during active Focus/Bedtime sessions.
5. **User Control & Deletion**: You can export your data (CSV, JSON, or AES-256-GCM encrypted backup) or permanently wipe all FocusNest data anytime under **More > Settings > Delete All Local FocusNest Data**.
