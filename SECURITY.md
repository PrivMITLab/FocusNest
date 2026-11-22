# Security Policy

FocusNest is offline-first and collects zero user data.

## Reporting a Vulnerability
Please report security issues responsibly via the repository Security tab or by emailing the maintainers without publishing exploit details before a fix is released.

## Security Guarantees
- Standard cryptography only (`javax.crypto` AES-256-GCM, Android Keystore, SHA-256).
- Parameterized Room SQLite queries via `@Dao` annotations.
- Zero Dynamic Code Loading (DCL) and zero third-party ad/analytics SDKs.
