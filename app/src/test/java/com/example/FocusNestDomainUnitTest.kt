package com.example

import com.example.core.common.KeystoreAndBackupCrypto
import com.example.domain.model.FocusTimeUtils
import com.example.domain.model.RulePriorityResolver
import com.example.domain.model.UnlockChallengeEngine
import com.example.domain.model.UnlockChallengeType
import com.example.service.FocusNestVpnService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FocusNestDomainUnitTest {

  @Test
  fun unlockChallengeEngine_validatesMathSentenceIntentionAndLimits() {
    val math = UnlockChallengeEngine.generateMathChallenge(attemptCountToday = 4)
    assertEquals(2, math.difficultyLevel)

    assertTrue(UnlockChallengeEngine.validateSentence("Protect focus now.", "Protect focus now."))
    assertFalse(UnlockChallengeEngine.validateSentence("Protect focus now.", "protect focus"))

    assertTrue(UnlockChallengeEngine.validateIntention("Checking calendar schedule for meeting"))
    assertFalse(UnlockChallengeEngine.validateIntention("bored"))

    val (dupAllowed, dupMsg) =
      UnlockChallengeEngine.canGrantUnlock(
        unlocksUsedToday = 1,
        dailyUnlockLimit = 5,
        lastUnlockEpochMs = 100_000L,
        nowEpochMs = 103_000L,
      )
    assertFalse(dupAllowed)
    assertNotNull(dupMsg)

    val (capAllowed, _) =
      UnlockChallengeEngine.canGrantUnlock(
        unlocksUsedToday = 5,
        dailyUnlockLimit = 5,
        lastUnlockEpochMs = 10_000L,
        nowEpochMs = 100_000L,
      )
    assertFalse(capAllowed)
  }

  @Test
  fun rulePriorityResolver_respectsPredictableHierarchy() {
    val tempUnlocked =
      RulePriorityResolver.resolve(
        packageName = "com.app",
        tempUnlockExpiresAtEpochMs = 200_000L,
        nowEpochMs = 100_000L,
        activeFocusBlocksPackage = true,
        activeFocusProfileName = "Work",
        matchingScheduledProfileName = "Study",
        matchingScheduledProfileId = 1L,
        matchingScheduledPriority = 8,
        matchingChallenge = UnlockChallengeType.ADAPTIVE_MATH,
        isStrictMode = true,
        usedMinutesToday = 90,
        dailyLimitMinutes = 30,
      )
    assertFalse(tempUnlocked.isBlocked)

    val focusWinsOverDailyLimit =
      RulePriorityResolver.resolve(
        packageName = "com.app",
        tempUnlockExpiresAtEpochMs = null,
        nowEpochMs = 100_000L,
        activeFocusBlocksPackage = true,
        activeFocusProfileName = "Deep Work",
        matchingScheduledProfileName = null,
        matchingScheduledProfileId = null,
        matchingScheduledPriority = 0,
        matchingChallenge = UnlockChallengeType.INTENTION_ENTRY,
        isStrictMode = false,
        usedMinutesToday = 60,
        dailyLimitMinutes = 30,
      )
    assertTrue(focusWinsOverDailyLimit.isBlocked)
    assertEquals(90, focusWinsOverDailyLimit.priorityRank)
  }

  @Test
  fun vpnDomainMatcher_andDnsPacketParser_workLocally() {
    assertTrue(FocusNestVpnService.domainMatches("m.youtube.com", "youtube.com"))
    assertTrue(FocusNestVpnService.domainMatches("youtube.com", "youtube.com"))
    assertFalse(FocusNestVpnService.domainMatches("notyoutube.com", "youtube.com"))

    // Minimal IPv4 UDP port 53 DNS query for "a.com"
    val pkt =
      byteArrayOf(
        0x45, 0x00, 0x00, 0x25, 0x00, 0x01, 0x00, 0x00, 0x40, 0x11, 0x00, 0x00,
        10, 88, 0, 2, 10, 88, 0, 1,
        0x30, 0x39, 0x00, 0x35, 0x00, 0x11, 0x00, 0x00,
        0x12, 0x34, 0x01, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        1, 'a'.code.toByte(), 3, 'c'.code.toByte(), 'o'.code.toByte(), 'm'.code.toByte(), 0,
      )
    val parsed = FocusNestVpnService.parseDnsQueryDomain(pkt, pkt.size)
    assertEquals("a.com", parsed)
    val nx = FocusNestVpnService.buildDnsNxdomainResponse(pkt, pkt.size)
    assertNotNull(nx)
    assertEquals(0x80, nx!![30].toInt() and 0x80) // QR=1
    assertEquals(0x03, nx[31].toInt() and 0x0F) // RCODE=NXDOMAIN
  }

  @Test
  fun portableAesGcmEncryption_roundTripsAndFormatsTime() {
    val encrypted = KeystoreAndBackupCrypto.encryptPortableBackup("{\"hello\":\"focus\"}", "strongpass")
    val decrypted = KeystoreAndBackupCrypto.decryptPortableBackup(encrypted, "strongpass")
    assertEquals("{\"hello\":\"focus\"}", decrypted)
    assertEquals("1h 25m", FocusTimeUtils.formatDurationMinutes(85))
    assertEquals("02:05", FocusTimeUtils.formatTimerSeconds(125))
  }
}
