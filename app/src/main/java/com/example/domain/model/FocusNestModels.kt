package com.example.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max
import kotlin.random.Random

enum class SessionType(val label: String, val defaultMinutes: Int) {
  STUDY("Study", 45),
  WORK("Work", 50),
  READING("Reading", 30),
  CODING("Coding", 60),
  CREATIVE("Creative", 40),
  CUSTOM("Custom", 25),
}

enum class TimerMode(val label: String) {
  POMODORO("Pomodoro"),
  COUNTDOWN("Countdown"),
  STOPWATCH("Stopwatch"),
}

enum class TimerPhase(val label: String) {
  IDLE("Ready"),
  FOCUS("Focus"),
  SHORT_BREAK("Short Break"),
  LONG_BREAK("Long Break"),
}

enum class UnlockChallengeType(val label: String, val summary: String) {
  STRICT_MODE("Strict Mode", "No bypass until schedule or session ends (owner emergency only)"),
  FIXED_COOLDOWN("Fixed Cooldown", "Wait a mandatory cooldown countdown before opening"),
  TEMP_UNLOCK_DURATION("Temporary Unlock", "Choose a short 1–15 min mindful unlock window"),
  QR_SCAN("QR Code Scan", "Scan a registered FocusNest QR code offline"),
  BARCODE_SCAN("Barcode Scan", "Scan a registered product/book barcode token"),
  NFC_SCAN("NFC Tag Tap", "Tap a physical NFC tag paired to this profile"),
  ADAPTIVE_MATH("Adaptive Math", "Solve multi-step arithmetic calibrated to attempt count"),
  SENTENCE_TYPING("Sentence Typing", "Type an intentional commitment sentence verbatim"),
  INTENTION_ENTRY("Personal Intention", "Write why you need this app right now"),
  DAILY_UNLOCK_LIMIT("Daily Unlock Limit", "Caps maximum unlocks per app per day"),
  REFLECTION_REMINDER("Reflection Pause", "Guided breathing & reflection prompt before entry"),
}

enum class AppCategory(val label: String) {
  SOCIAL("Social"),
  VIDEO("Video & Shorts"),
  GAMING("Gaming"),
  PRODUCTIVITY("Productivity"),
  EDUCATION("Education"),
  COMMUNICATION("Communication"),
  BROWSER("Browsers"),
  OTHER("Other"),
}

enum class AccentPalette(val label: String) {
  EMERALD_SAGE("Sage Sanctuary"),
  WARM_AMBER("Warm Amber"),
  OCEAN_SLATE("Ocean Slate"),
  LAVENDER_DUSK("Calm Dusk"),
  TERRACOTTA_CALM("Earthy Clay"),
}

enum class ThemeMode(val label: String) {
  SYSTEM("System"),
  LIGHT("Light"),
  DARK("Dark"),
}

enum class ReelsTargetApp(
  val packageName: String,
  val displayName: String,
  val featureName: String,
  val viewIdHints: List<String>,
) {
  INSTAGRAM_REELS(
    packageName = "com.instagram.android",
    displayName = "Instagram",
    featureName = "Reels",
    viewIdHints = listOf("clips_viewer", "reel_viewer", "clips_video_container"),
  ),
  YOUTUBE_SHORTS(
    packageName = "com.google.android.youtube",
    displayName = "YouTube",
    featureName = "Shorts",
    viewIdHints = listOf("reel_watch_player", "shorts_player", "reel_recycler"),
  ),
  FACEBOOK_REELS(
    packageName = "com.facebook.katana",
    displayName = "Facebook",
    featureName = "Reels",
    viewIdHints = listOf("reels_viewer", "fb_shorts"),
  ),
  TIKTOK(
    packageName = "com.zhiliaoapp.musically",
    displayName = "TikTok",
    featureName = "For You Feed",
    viewIdHints = listOf("viewpager", "feed_item"),
  ),
}

data class PermissionSnapshot(
  val usageStatsGranted: Boolean = false,
  val accessibilityEnabled: Boolean = false,
  val notificationListenerEnabled: Boolean = false,
  val vpnPrepared: Boolean = false,
  val vpnActive: Boolean = false,
  val postNotificationsGranted: Boolean = true,
  val cameraGranted: Boolean = false,
  val dndPolicyAccessGranted: Boolean = false,
  val ignoringBatteryOptimizations: Boolean = false,
  val nfcSupported: Boolean = false,
  val nfcEnabled: Boolean = false,
)

data class ActiveBlockDecision(
  val isBlocked: Boolean,
  val reason: String,
  val profileId: Long? = null,
  val profileName: String? = null,
  val priorityRank: Int = 0,
  val challengeType: UnlockChallengeType = UnlockChallengeType.REFLECTION_REMINDER,
  val remainingAllowanceMinutes: Int? = null,
)

data class MathChallenge(
  val prompt: String,
  val expectedAnswer: Int,
  val difficultyLevel: Int,
)

object UnlockChallengeEngine {
  private val commitmentSentences =
    listOf(
      "I choose intentional presence over mindless scrolling right now.",
      "My attention is valuable and I protect my deep work time.",
      "One conscious pause keeps me aligned with my goals today.",
      "I am opening this app deliberately and will close it on time.",
    )

  fun generateMathChallenge(attemptCountToday: Int, random: Random = Random.Default): MathChallenge {
    val level = (attemptCountToday / 2).coerceIn(1, 4)
    return when (level) {
      1 -> {
        val a = random.nextInt(12, 35)
        val b = random.nextInt(11, 29)
        MathChallenge("$a + $b = ?", a + b, level)
      }
      2 -> {
        val a = random.nextInt(40, 95)
        val b = random.nextInt(12, 38)
        val c = random.nextInt(3, 9)
        MathChallenge("($a - $b) × $c = ?", (a - b) * c, level)
      }
      3 -> {
        val a = random.nextInt(11, 24)
        val b = random.nextInt(6, 15)
        val c = random.nextInt(15, 45)
        MathChallenge("$a × $b + $c = ?", a * b + c, level)
      }
      else -> {
        val a = random.nextInt(14, 29)
        val b = random.nextInt(12, 22)
        val c = random.nextInt(25, 65)
        MathChallenge("$a × $b - $c = ?", a * b - c, level)
      }
    }
  }

  fun pickSentence(seedIndex: Int): String =
    commitmentSentences[Math.abs(seedIndex) % commitmentSentences.size]

  fun validateSentence(expected: String, typed: String): Boolean =
    expected.trim().equals(typed.trim(), ignoreCase = false)

  fun validateIntention(intention: String, minWords: Int = 3): Boolean {
    val words = intention.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return words.size >= minWords && intention.trim().length >= 12
  }

  fun canGrantUnlock(
    unlocksUsedToday: Int,
    dailyUnlockLimit: Int,
    lastUnlockEpochMs: Long?,
    nowEpochMs: Long,
    minDuplicateGuardMs: Long = 10_000L,
  ): Pair<Boolean, String?> {
    if (lastUnlockEpochMs != null && (nowEpochMs - lastUnlockEpochMs) < minDuplicateGuardMs) {
      return false to "Duplicate unlock prevented. Wait a moment before retrying."
    }
    if (dailyUnlockLimit > 0 && unlocksUsedToday >= dailyUnlockLimit) {
      return false to "Daily unlock limit ($dailyUnlockLimit) reached for this rule."
    }
    return true to null
  }
}

object RulePriorityResolver {
  /**
   * Predictable deterministic rule priority:
   * 100 - Active temporary unlock window (explicitly unlocked) -> ALLOW
   * 90  - Active Focus Session blocking
   * 80  - Invincible / Strict manual block
   * 70  - Active Scheduled Profile (sorted by profile priority)
   * 60  - App or Group Daily/Weekly Limit exceeded
   */
  fun resolve(
    packageName: String,
    tempUnlockExpiresAtEpochMs: Long?,
    nowEpochMs: Long,
    activeFocusBlocksPackage: Boolean,
    activeFocusProfileName: String?,
    matchingScheduledProfileName: String?,
    matchingScheduledProfileId: Long?,
    matchingScheduledPriority: Int,
    matchingChallenge: UnlockChallengeType,
    isStrictMode: Boolean,
    usedMinutesToday: Int,
    dailyLimitMinutes: Int?,
    usedMinutesWeek: Int = 0,
    weeklyLimitMinutes: Int? = null,
  ): ActiveBlockDecision {
    if (tempUnlockExpiresAtEpochMs != null && tempUnlockExpiresAtEpochMs > nowEpochMs) {
      val remMin = max(1, ((tempUnlockExpiresAtEpochMs - nowEpochMs) / 60_000L).toInt())
      return ActiveBlockDecision(
        isBlocked = false,
        reason = "Temporary unlock active ($remMin min left)",
        remainingAllowanceMinutes = remMin,
      )
    }

    if (activeFocusBlocksPackage) {
      return ActiveBlockDecision(
        isBlocked = true,
        reason = "Blocked by active Focus session",
        profileName = activeFocusProfileName ?: "Focus Session",
        priorityRank = 90,
        challengeType = if (isStrictMode) UnlockChallengeType.STRICT_MODE else matchingChallenge,
      )
    }

    if (matchingScheduledProfileName != null) {
      return ActiveBlockDecision(
        isBlocked = true,
        reason = "Restricted by profile: $matchingScheduledProfileName",
        profileId = matchingScheduledProfileId,
        profileName = matchingScheduledProfileName,
        priorityRank = 70 + matchingScheduledPriority.coerceIn(0, 19),
        challengeType = if (isStrictMode) UnlockChallengeType.STRICT_MODE else matchingChallenge,
      )
    }

    if (dailyLimitMinutes != null && dailyLimitMinutes > 0 && usedMinutesToday >= dailyLimitMinutes) {
      return ActiveBlockDecision(
        isBlocked = true,
        reason = "Daily limit reached (${usedMinutesToday}m / ${dailyLimitMinutes}m)",
        priorityRank = 60,
        challengeType = if (isStrictMode) UnlockChallengeType.STRICT_MODE else matchingChallenge,
        remainingAllowanceMinutes = 0,
      )
    }

    if (weeklyLimitMinutes != null && weeklyLimitMinutes > 0 && usedMinutesWeek >= weeklyLimitMinutes) {
      return ActiveBlockDecision(
        isBlocked = true,
        reason = "Weekly limit reached (${usedMinutesWeek}m / ${weeklyLimitMinutes}m)",
        priorityRank = 55,
        challengeType = if (isStrictMode) UnlockChallengeType.STRICT_MODE else matchingChallenge,
        remainingAllowanceMinutes = 0,
      )
    }

    val remaining =
      if (dailyLimitMinutes != null && dailyLimitMinutes > 0) {
        max(0, dailyLimitMinutes - usedMinutesToday)
      } else {
        null
      }

    return ActiveBlockDecision(
      isBlocked = false,
      reason = "Allowed",
      remainingAllowanceMinutes = remaining,
    )
  }
}

object FocusTimeUtils {
  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

  fun todayKey(nowEpochMs: Long = System.currentTimeMillis()): String =
    dateKeyFromEpoch(nowEpochMs)

  fun dateKeyFromEpoch(epochMs: Long): String =
    Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).toLocalDate().format(dateFormatter)

  fun dateKeyDaysAgo(daysAgo: Int): String =
    LocalDate.now().minusDays(daysAgo.toLong()).format(dateFormatter)

  fun formatDurationMinutes(totalMinutes: Int): String {
    val safe = max(0, totalMinutes)
    val h = safe / 60
    val m = safe % 60
    return when {
      h > 0 && m > 0 -> "${h}h ${m}m"
      h > 0 -> "${h}h"
      else -> "${m}m"
    }
  }

  fun formatDurationMillis(millis: Long): String {
    val totalMinutes = (max(0L, millis) / 60_000L).toInt()
    return formatDurationMinutes(totalMinutes)
  }

  fun formatTimerSeconds(totalSeconds: Long): String {
    val safe = max(0L, totalSeconds)
    val hours = safe / 3600
    val minutes = (safe % 3600) / 60
    val seconds = safe % 60
    return if (hours > 0) {
      String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
      String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
  }

  fun formatMinuteOfDay(minuteOfDay: Int): String {
    val clamped = minuteOfDay.coerceIn(0, 1439)
    val h = clamped / 60
    val m = clamped % 60
    return String.format(Locale.US, "%02d:%02d", h, m)
  }
}
