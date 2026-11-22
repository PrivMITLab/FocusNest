package com.example

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Process
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.core.permissions.PermissionManager
import com.example.service.BootCompletedReceiver
import com.example.ui.FocusNestViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun appName_isFocusNest() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertEquals("FocusNest", context.getString(R.string.app_name))
  }

  @Test
  fun permissionChecks_handleMissingPermissionsAndNoNfcGracefully() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    Shadows.shadowOf(appOps)
      .setMode(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName,
        AppOpsManager.MODE_IGNORED,
      )
    val snapshot = PermissionManager.getSnapshot(context)
    assertFalse(snapshot.usageStatsGranted)
    assertFalse(snapshot.accessibilityEnabled)
    assertFalse(snapshot.notificationListenerEnabled)
    assertFalse(snapshot.vpnActive)
    assertFalse(snapshot.nfcSupported)
    runCatching { VpnService.prepare(context) }
  }

  @Test
  fun bootCompletedReceiver_handlesBootWithoutCrash() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val receiver = BootCompletedReceiver()
    receiver.onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))
  }

  @Test
  fun onboardingToDashboard_andFiveTabNavigation_works() {
    val app = ApplicationProvider.getApplicationContext<FocusNestApplication>()
    val vm = FocusNestViewModel(app)

    composeTestRule.setContent {
      FocusNestAppRoot(viewModel = vm, useNavRail = false)
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Welcome to FocusNest").assertIsDisplayed()
    composeTestRule.onNodeWithTag("onboarding_skip_all_btn").performClick()

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithText("Today's Screen Time").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("Today's Screen Time").assertIsDisplayed()

    composeTestRule.onNodeWithTag("nav_focus").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Focus Sanctuary").assertIsDisplayed()

    composeTestRule.onNodeWithTag("nav_blocker").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Shield & Blocker").assertIsDisplayed()

    composeTestRule.onNodeWithTag("nav_insights").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Insights & Reports").assertIsDisplayed()

    composeTestRule.onNodeWithTag("nav_more").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("More & Sanctuary Controls").assertIsDisplayed()
  }
}
