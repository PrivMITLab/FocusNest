package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.AppGroupEntity
import com.example.core.database.AppLimitEntity
import com.example.core.database.AppUsageEntity
import com.example.core.database.BlockProfileEntity
import com.example.core.database.FocusNestDatabase
import com.example.core.database.FocusSessionEntity
import com.example.core.database.InstalledAppEntity
import com.example.core.database.WebsiteRuleEntity
import com.example.core.datastore.FocusNestPreferences
import com.example.core.datastore.PersistedTimerState
import com.example.data.repository.BlockerRepository
import com.example.data.repository.FocusRepository
import com.example.data.repository.InsightsAndBackupRepository
import com.example.domain.model.FocusTimeUtils
import com.example.domain.model.SessionType
import com.example.domain.model.TimerMode
import com.example.domain.model.TimerPhase
import com.example.domain.model.UnlockChallengeType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FocusNestDatabaseAndRepoTest {

  private lateinit var context: Context
  private lateinit var db: FocusNestDatabase
  private lateinit var prefs: FocusNestPreferences
  private lateinit var blockerRepo: BlockerRepository
  private lateinit var focusRepo: FocusRepository
  private lateinit var insightsRepo: InsightsAndBackupRepository

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    db = Room.inMemoryDatabaseBuilder(context, FocusNestDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    prefs = FocusNestPreferences(context)
    blockerRepo =
      BlockerRepository(
        appLimitDao = db.appLimitDao(),
        appGroupDao = db.appGroupDao(),
        blockProfileDao = db.blockProfileDao(),
        blockingScheduleDao = db.blockingScheduleDao(),
        unlockDao = db.unlockDao(),
        websiteRuleDao = db.websiteRuleDao(),
        tokenVaultDao = db.tokenVaultDao(),
        shortFormRuleDao = db.shortFormRuleDao(),
        appUsageDao = db.appUsageDao(),
        dailyAnalyticsDao = db.dailyAnalyticsDao(),
        installedAppDao = db.installedAppDao(),
        preferences = prefs,
      )
    focusRepo = FocusRepository(db.focusSessionDao(), db.focusGoalDao(), db.dailyAnalyticsDao())
    insightsRepo =
      InsightsAndBackupRepository(
        appUsageDao = db.appUsageDao(),
        dailyAnalyticsDao = db.dailyAnalyticsDao(),
        focusSessionDao = db.focusSessionDao(),
        focusGoalDao = db.focusGoalDao(),
        appLimitDao = db.appLimitDao(),
        appGroupDao = db.appGroupDao(),
        blockProfileDao = db.blockProfileDao(),
        blockingScheduleDao = db.blockingScheduleDao(),
        websiteRuleDao = db.websiteRuleDao(),
        unlockDao = db.unlockDao(),
        reminderDao = db.reminderDao(),
        notificationRecordDao = db.notificationRecordDao(),
        preferences = prefs,
      )
  }

  @After
  fun teardown() {
    db.close()
  }

  @Test
  fun blockerRepository_evaluatesLimitsGroupsAndTemporaryUnlocks() = runTest {
    val pkg = "com.social.demo"
    val today = FocusTimeUtils.todayKey()
    db.installedAppDao().upsertAll(
      listOf(InstalledAppEntity(pkg, "Demo Social", "SOCIAL", isEmergencyWhitelisted = false))
    )
    db.appUsageDao().upsertAll(
      listOf(AppUsageEntity(today, pkg, "Demo Social", "SOCIAL", foregroundTimeMs = 35 * 60_000L, launchCount = 5))
    )
    blockerRepo.saveAppGroup(
      AppGroupEntity(name = "Social", packageNames = listOf(pkg), sharedDailyLimitMinutes = 30)
    )

    val blockedDecision = blockerRepo.evaluatePackageDecision(pkg)
    assertTrue(blockedDecision.isBlocked)

    val (granted, _) =
      blockerRepo.grantTemporaryUnlock(
        packageName = pkg,
        appName = "Demo Social",
        challengeType = UnlockChallengeType.ADAPTIVE_MATH,
        durationMinutes = 5,
      )
    assertTrue(granted)

    val afterUnlock = blockerRepo.evaluatePackageDecision(pkg)
    assertFalse(afterUnlock.isBlocked)
  }

  @Test
  fun focusRepository_calculatesStreakAcrossConsecutiveDays() = runTest {
    val today = FocusTimeUtils.todayKey()
    val yesterday = FocusTimeUtils.dateKeyDaysAgo(1)
    val now = System.currentTimeMillis()
    focusRepo.recordFocusSession(
      FocusSessionEntity(
        dateKey = today,
        sessionType = SessionType.STUDY.name,
        timerMode = TimerMode.POMODORO.name,
        plannedMinutes = 25,
        elapsedSeconds = 1500,
        completed = true,
        startedAtEpochMs = now - 1500_000,
        endedAtEpochMs = now,
      )
    )
    focusRepo.recordFocusSession(
      FocusSessionEntity(
        dateKey = yesterday,
        sessionType = SessionType.CODING.name,
        timerMode = TimerMode.COUNTDOWN.name,
        plannedMinutes = 40,
        elapsedSeconds = 2400,
        completed = true,
        startedAtEpochMs = now - 86_400_000,
        endedAtEpochMs = now - 84_000_000,
      )
    )

    val sessions = db.focusSessionDao().getAll()
    assertEquals(2, focusRepo.computeCurrentStreakDays(sessions))
  }

  @Test
  fun encryptedBackupAndRestore_roundTripsDatabaseRecords() = runTest {
    blockerRepo.saveProfile(
      BlockProfileEntity(name = "Night Study", presetType = "Study", blockedPackages = listOf("com.video.app"))
    )
    blockerRepo.saveAppLimit(
      AppLimitEntity("com.video.app", "Video App", dailyLimitMinutes = 20)
    )
    blockerRepo.saveWebsiteRule(WebsiteRuleEntity(domain = "example.com", category = "Test"))

    val encrypted = insightsRepo.exportEncryptedBackup("secret-pass-2026")
    assertTrue(encrypted.contains("focusnest-encrypted-backup"))

    insightsRepo.deleteAllUserData()
    assertTrue(db.blockProfileDao().getAll().isEmpty())

    val restoredCount = insightsRepo.restoreFromBackup(encrypted, "secret-pass-2026")
    assertTrue(restoredCount >= 3)
    assertEquals("Night Study", db.blockProfileDao().getAll().first().name)
    assertEquals("example.com", db.websiteRuleDao().getAll().first().domain)
  }

  @Test
  fun timerStatePersistence_survivesReload() = runTest {
    val saved =
      PersistedTimerState(
        isRunning = true,
        phase = TimerPhase.FOCUS,
        mode = TimerMode.POMODORO,
        sessionType = SessionType.CODING,
        plannedMinutes = 50,
        targetEndEpochMs = 1_900_000_000_000L,
        startedAtEpochMs = 1_899_997_000_000L,
        completedCycles = 1,
        sessionNote = "Kernel module",
      )
    prefs.saveTimerState(saved)
    val loaded = prefs.timerStateFlow.first()
    assertTrue(loaded.isRunning)
    assertEquals(SessionType.CODING, loaded.sessionType)
    assertEquals("Kernel module", loaded.sessionNote)
    assertEquals(1_900_000_000_000L, loaded.targetEndEpochMs)
  }
}
