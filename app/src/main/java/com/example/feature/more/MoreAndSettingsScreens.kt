package com.example.feature.more

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.NotificationsPaused
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.core.common.OemCompatibilityHelper
import com.example.core.common.QrCodeGenerator
import com.example.core.database.BlockProfileEntity
import com.example.core.database.FocusGoalEntity
import com.example.core.database.NfcTagEntity
import com.example.core.database.NotificationRecordEntity
import com.example.core.database.QrTokenEntity
import com.example.core.datastore.FocusNestSettings
import com.example.core.design.FocusNestEmptyState
import com.example.core.design.SanctuaryCard
import com.example.core.design.SectionHeader
import com.example.core.permissions.PermissionManager
import com.example.domain.model.AccentPalette
import com.example.domain.model.FocusTimeUtils
import com.example.domain.model.PermissionSnapshot
import com.example.domain.model.ThemeMode
import com.example.feature.challenges.OfflineQrBarcodeScannerDialog
import java.util.UUID
import kotlinx.coroutines.launch

enum class MoreSubPage {
  HUB,
  VAULT,
  INVINCIBLE,
  MINDFUL,
  NOTIFICATIONS,
  PERMISSIONS,
  SETTINGS,
  PRIVACY_AND_ABOUT,
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoreScreen(
  settings: FocusNestSettings,
  permissions: PermissionSnapshot,
  focusGoal: FocusGoalEntity,
  profiles: List<BlockProfileEntity>,
  nfcTags: List<NfcTagEntity>,
  qrTokens: List<QrTokenEntity>,
  lastScannedNfcUid: String?,
  recentNotifications: List<NotificationRecordEntity>,
  onRefreshPermissions: () -> Unit,
  onSaveNfcTag: (String, String, String, Long?, Int) -> Unit,
  onDeleteNfcTag: (NfcTagEntity) -> Unit,
  onSaveQrToken: (String, String, String, Long?) -> Unit,
  onDeleteQrToken: (QrTokenEntity) -> Unit,
  onEnableInvincible: (Int, Boolean, String) -> Unit,
  onRequestInvincibleUnlock: () -> Unit,
  onCompleteInvincibleDisable: () -> Boolean,
  onEmergencyDisableInvincible: (String) -> Boolean,
  onUpdateMindful: (
    bedtimeEnabled: Boolean?,
    bedtimeStart: Int?,
    bedtimeEnd: Int?,
    bedtimeDnd: Boolean?,
    breakEnabled: Boolean?,
    breakInterval: Int?,
    notifFilterEnabled: Boolean?,
    cooldownSec: Int?,
  ) -> Unit,
  onUpdateAppearance: (ThemeMode?, AccentPalette?, Boolean?, String?) -> Unit,
  onUpdateGoals: (Int, Int, Int, Int) -> Unit,
  onBuildEncryptedBackup: suspend (String) -> String,
  onRestoreBackup: suspend (String, String?) -> Result<Int>,
  onDeleteAllData: () -> Unit,
  onShowMessage: (String) -> Unit,
) {
  var subPage by rememberSaveable { mutableStateOf(MoreSubPage.HUB) }

  if (subPage != MoreSubPage.HUB) {
    BackHandler { subPage = MoreSubPage.HUB }
  }

  when (subPage) {
    MoreSubPage.HUB -> {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        item {
          Text("More & Sanctuary Controls", style = MaterialTheme.typography.headlineMedium)
          Text(
            "NFC/QR tokens, Invincible Mode, Mindful Tools, Permissions & Backups",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        item {
          MoreHubTile(
            icon = Icons.Default.Nfc,
            title = "NFC, QR & Barcode Vault",
            subtitle = "${nfcTags.size} NFC tags • ${qrTokens.size} QR/Barcode tokens",
            tag = "more_tile_vault",
            onClick = { subPage = MoreSubPage.VAULT },
          )
        }
        item {
          MoreHubTile(
            icon = Icons.Default.Lock,
            title = "Invincible Mode",
            subtitle = if (settings.invincibleModeEnabled) "Active (${settings.invincibleDelayMinutes}m lock delay)" else "Off • Optional delayed rule lock & biometric gate",
            tag = "more_tile_invincible",
            onClick = { subPage = MoreSubPage.INVINCIBLE },
          )
        }
        item {
          MoreHubTile(
            icon = Icons.Default.Bedtime,
            title = "Mindful Usage Tools",
            subtitle = "Bedtime, Scheduled DND, Break Reminders & Cooldowns",
            tag = "more_tile_mindful",
            onClick = { subPage = MoreSubPage.MINDFUL },
          )
        }
        item {
          MoreHubTile(
            icon = Icons.Default.NotificationsPaused,
            title = "Notification Management",
            subtitle = if (settings.notificationFilterEnabled) "Local focus filtering enabled" else "Optional local notification log & muting",
            tag = "more_tile_notifications",
            onClick = { subPage = MoreSubPage.NOTIFICATIONS },
          )
        }
        item {
          MoreHubTile(
            icon = Icons.Default.AdminPanelSettings,
            title = "Permissions & Device Compatibility",
            subtitle = "Transparent status for Android 11–16 & Realme/OEM optimization",
            tag = "more_tile_permissions",
            onClick = { subPage = MoreSubPage.PERMISSIONS },
          )
        }
        item {
          MoreHubTile(
            icon = Icons.Default.Settings,
            title = "Settings, Goals & Encrypted Backup",
            subtitle = "Theme, accents, goals, AES-256-GCM backup & restore",
            tag = "more_tile_settings",
            onClick = { subPage = MoreSubPage.SETTINGS },
          )
        }
        item {
          MoreHubTile(
            icon = Icons.Default.Security,
            title = "Privacy Policy, Threat Model & Licenses",
            subtitle = "Offline-first guarantees & open-source credits",
            tag = "more_tile_privacy",
            onClick = { subPage = MoreSubPage.PRIVACY_AND_ABOUT },
          )
        }
      }
    }

    MoreSubPage.VAULT ->
      VaultSubPage(
        permissions = permissions,
        profiles = profiles,
        nfcTags = nfcTags,
        qrTokens = qrTokens,
        lastScannedNfcUid = lastScannedNfcUid,
        onBack = { subPage = MoreSubPage.HUB },
        onSaveNfcTag = onSaveNfcTag,
        onDeleteNfcTag = onDeleteNfcTag,
        onSaveQrToken = onSaveQrToken,
        onDeleteQrToken = onDeleteQrToken,
      )

    MoreSubPage.INVINCIBLE ->
      InvincibleSubPage(
        settings = settings,
        onBack = { subPage = MoreSubPage.HUB },
        onEnableInvincible = onEnableInvincible,
        onRequestUnlock = onRequestInvincibleUnlock,
        onCompleteDisable = onCompleteInvincibleDisable,
        onEmergencyDisable = onEmergencyDisableInvincible,
        onShowMessage = onShowMessage,
      )

    MoreSubPage.MINDFUL ->
      MindfulSubPage(
        settings = settings,
        permissions = permissions,
        onBack = { subPage = MoreSubPage.HUB },
        onUpdateMindful = onUpdateMindful,
        onRefreshPermissions = onRefreshPermissions,
      )

    MoreSubPage.NOTIFICATIONS ->
      NotificationsSubPage(
        settings = settings,
        permissions = permissions,
        records = recentNotifications,
        onBack = { subPage = MoreSubPage.HUB },
        onToggleFilter = { enabled ->
          onUpdateMindful(null, null, null, null, null, null, enabled, null)
        },
      )

    MoreSubPage.PERMISSIONS ->
      PermissionsSubPage(
        permissions = permissions,
        onBack = { subPage = MoreSubPage.HUB },
        onRefresh = onRefreshPermissions,
      )

    MoreSubPage.SETTINGS ->
      SettingsSubPage(
        settings = settings,
        focusGoal = focusGoal,
        onBack = { subPage = MoreSubPage.HUB },
        onUpdateAppearance = onUpdateAppearance,
        onUpdateGoals = onUpdateGoals,
        onBuildEncryptedBackup = onBuildEncryptedBackup,
        onRestoreBackup = onRestoreBackup,
        onDeleteAllData = onDeleteAllData,
        onShowMessage = onShowMessage,
      )

    MoreSubPage.PRIVACY_AND_ABOUT ->
      PrivacyAndAboutSubPage(onBack = { subPage = MoreSubPage.HUB })
  }
}

@Composable
private fun MoreHubTile(
  icon: ImageVector,
  title: String,
  subtitle: String,
  tag: String,
  onClick: () -> Unit,
) {
  SanctuaryCard(modifier = Modifier.testTag(tag).clickable(onClick = onClick)) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  }
}

@Composable
private fun SubPageHeader(title: String, onBack: () -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    IconButton(onClick = onBack, modifier = Modifier.testTag("subpage_back_btn")) {
      Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
    }
    Text(title, style = MaterialTheme.typography.headlineMedium)
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VaultSubPage(
  permissions: PermissionSnapshot,
  profiles: List<BlockProfileEntity>,
  nfcTags: List<NfcTagEntity>,
  qrTokens: List<QrTokenEntity>,
  lastScannedNfcUid: String?,
  onBack: () -> Unit,
  onSaveNfcTag: (String, String, String, Long?, Int) -> Unit,
  onDeleteNfcTag: (NfcTagEntity) -> Unit,
  onSaveQrToken: (String, String, String, Long?) -> Unit,
  onDeleteQrToken: (QrTokenEntity) -> Unit,
) {
  val context = LocalContext.current
  var showScanner by remember { mutableStateOf(false) }
  var qrPreviewToken by remember { mutableStateOf<String?>(null) }
  var nfcUidInput by remember(lastScannedNfcUid) { mutableStateOf(lastScannedNfcUid ?: "") }
  var nfcLabel by remember { mutableStateOf("Desk Study Tag") }
  var nfcAction by remember { mutableStateOf("START_FOCUS") }
  var qrLabel by remember { mutableStateOf("Physical Study Card") }

  Column(modifier = Modifier.fillMaxSize()) {
    SubPageHeader("NFC, QR & Barcode Vault", onBack)
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item {
        SanctuaryCard {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Nfc, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text =
                  when {
                    !permissions.nfcSupported -> "NFC Hardware Not Detected on This Device"
                    !permissions.nfcEnabled -> "NFC Supported • Currently Disabled in Android Settings"
                    else -> "NFC Ready • Tap any NFC tag against device back"
                  },
                style = MaterialTheme.typography.titleMedium,
              )
            }
            if (permissions.nfcSupported && !permissions.nfcEnabled) {
              OutlinedButton(
                onClick = { PermissionManager.safeStartSettings(context, PermissionManager.nfcSettingsIntent()) }
              ) { Text("Enable NFC in Settings") }
            }
            OutlinedTextField(
              value = nfcUidInput,
              onValueChange = { nfcUidInput = it },
              label = { Text("NFC Tag UID (tap tag or enter hex)") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
              value = nfcLabel,
              onValueChange = { nfcLabel = it },
              label = { Text("Tag Label") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              listOf("START_FOCUS" to "Start 25m Focus", "UNLOCK_PROFILE" to "Unlock Profile", "TOGGLE_BLOCKER" to "Toggle Shield").forEach { (act, lbl) ->
                FilterChip(selected = nfcAction == act, onClick = { nfcAction = act }, label = { Text(lbl) })
              }
            }
            Button(
              enabled = nfcUidInput.isNotBlank(),
              onClick = {
                onSaveNfcTag(nfcUidInput, nfcLabel, nfcAction, profiles.firstOrNull()?.id, 25)
                nfcUidInput = ""
              },
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text("Register NFC Tag")
            }
          }
        }
      }

      items(nfcTags, key = { it.tagUidHex }) { tag ->
        SanctuaryCard {
          Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(tag.label, style = MaterialTheme.typography.titleMedium)
              Text("${tag.tagUidHex} • ${tag.actionType}", style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = { onDeleteNfcTag(tag) }) {
              Icon(Icons.Default.Delete, contentDescription = "Delete NFC tag")
            }
          }
        }
      }

      item {
        SanctuaryCard {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Personal QR Codes & Barcodes (100% Offline)", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
              value = qrLabel,
              onValueChange = { qrLabel = it },
              label = { Text("Token Label") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                onClick = {
                  val token = "FOCUSNEST:${UUID.randomUUID().toString().take(8).uppercase()}"
                  onSaveQrToken(token, qrLabel, "QR_CODE", profiles.firstOrNull()?.id)
                  qrPreviewToken = token
                },
                modifier = Modifier.weight(1f),
              ) {
                Icon(Icons.Default.QrCode2, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate QR")
              }
              OutlinedButton(
                onClick = { showScanner = true },
                modifier = Modifier.weight(1f),
              ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Scan Token")
              }
            }
            qrPreviewToken?.let { code ->
              val bmp = remember(code) { QrCodeGenerator.generateQrBitmap(code, 360) }
              if (bmp != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                  Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Generated FocusNest unlock QR code",
                    modifier = Modifier.size(180.dp),
                  )
                  Text(code, style = MaterialTheme.typography.labelMedium)
                }
              }
            }
          }
        }
      }

      items(qrTokens, key = { it.id }) { token ->
        SanctuaryCard {
          Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f), modifier2 = Modifier) {
              Text(token.label, style = MaterialTheme.typography.titleMedium)
              Text("${token.tokenFormat} • ${token.tokenValue}", style = MaterialTheme.typography.bodyMedium)
            }
            Row {
              if (token.tokenFormat == "QR_CODE") {
                TextButton(onClick = { qrPreviewToken = token.tokenValue }) { Text("View") }
              }
              IconButton(onClick = { onDeleteQrToken(token) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete token")
              }
            }
          }
        }
      }
    }
  }

  if (showScanner) {
    OfflineQrBarcodeScannerDialog(
      title = "Register QR or Barcode",
      onCodeScanned = { code ->
        val fmt = if (code.all { it.isDigit() }) "BARCODE" else "QR_CODE"
        onSaveQrToken(code, qrLabel, fmt, profiles.firstOrNull()?.id)
        showScanner = false
      },
      onDismiss = { showScanner = false },
    )
  }
}

@Composable
private fun Column(modifier: Modifier = Modifier, modifier2: Modifier = Modifier, content: @Composable () -> Unit) {
  androidx.compose.foundation.layout.Column(modifier = modifier) { content() }
}

@Composable
private fun InvincibleSubPage(
  settings: FocusNestSettings,
  onBack: () -> Unit,
  onEnableInvincible: (Int, Boolean, String) -> Unit,
  onRequestUnlock: () -> Unit,
  onCompleteDisable: () -> Boolean,
  onEmergencyDisable: (String) -> Boolean,
  onShowMessage: (String) -> Unit,
) {
  val context = LocalContext.current
  var delayMinutes by remember { mutableIntStateOf(settings.invincibleDelayMinutes) }
  var requireBiometric by remember { mutableStateOf(settings.invincibleBiometricRequired) }
  var emergencyPin by remember { mutableStateOf("") }
  var recoveryPinInput by remember { mutableStateOf("") }

  fun launchBiometricThen(onAuthed: () -> Unit) {
    val activity = context as? FragmentActivity
    val canAuth =
      BiometricManager.from(context).canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
      ) == BiometricManager.BIOMETRIC_SUCCESS
    if (activity != null && canAuth) {
      val prompt =
        BiometricPrompt(
          activity,
          ContextCompat.getMainExecutor(context),
          object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
              onAuthed()
            }
          },
        )
      val info =
        BiometricPrompt.PromptInfo.Builder()
          .setTitle("Verify Owner Identity")
          .setSubtitle("Confirm before changing Invincible Mode protection")
          .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
          )
          .build()
      prompt.authenticate(info)
    } else {
      onAuthed()
    }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    SubPageHeader("Invincible Mode", onBack)
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item {
        SanctuaryCard(highlight = settings.invincibleModeEnabled) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              if (settings.invincibleModeEnabled) "Invincible Mode Active" else "Invincible Mode Standby",
              style = MaterialTheme.typography.titleLarge,
            )
            Text(
              "Locks active restriction rules behind a configurable delay and optional Biometric confirmation. FocusNest never abuses Device Admin or stealth-locks your phone—you always keep owner recovery.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      if (!settings.invincibleModeEnabled) {
        item {
          SanctuaryCard {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text("Rule-Change Delay: $delayMinutes minutes", style = MaterialTheme.typography.titleMedium)
              Slider(
                value = delayMinutes.toFloat(),
                onValueChange = { delayMinutes = (it.toInt() / 5) * 5 },
                valueRange = 5f..60f,
              )
              Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Require Biometric / Device Credential")
                Switch(checked = requireBiometric, onCheckedChange = { requireBiometric = it })
              }
              OutlinedTextField(
                value = emergencyPin,
                onValueChange = { emergencyPin = it },
                label = { Text("Owner Emergency Recovery Passcode") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
              )
              Button(
                onClick = {
                  if (requireBiometric) {
                    launchBiometricThen { onEnableInvincible(delayMinutes, true, emergencyPin) }
                  } else {
                    onEnableInvincible(delayMinutes, false, emergencyPin)
                  }
                },
                modifier = Modifier.fillMaxWidth().testTag("invincible_enable_btn"),
              ) {
                Text("Activate Invincible Mode")
              }
            }
          }
        }
      } else {
        item {
          SanctuaryCard {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              val pendingAt = settings.invinciblePendingUnlockAtEpochMs
              val now = System.currentTimeMillis()
              if (pendingAt == 0L) {
                Button(
                  onClick = {
                    if (settings.invincibleBiometricRequired) launchBiometricThen { onRequestUnlock() }
                    else onRequestUnlock()
                  },
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Text("Request Disable (${settings.invincibleDelayMinutes}m Cooldown)")
                }
              } else if (now < pendingAt) {
                val remMin = ((pendingAt - now) / 60_000L) + 1
                Text("Cooldown active: $remMin min remaining before rules unlock.")
              } else {
                Button(
                  onClick = { onCompleteDisable() },
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Text("Complete Disable Now")
                }
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text("Owner Emergency Recovery", style = MaterialTheme.typography.titleMedium)
              OutlinedTextField(
                value = recoveryPinInput,
                onValueChange = { recoveryPinInput = it },
                label = { Text("Emergency Recovery Passcode") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
              )
              OutlinedButton(
                onClick = { onEmergencyDisable(recoveryPinInput) },
                modifier = Modifier.fillMaxWidth(),
              ) {
                Text("Recover Immediately")
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun MindfulSubPage(
  settings: FocusNestSettings,
  permissions: PermissionSnapshot,
  onBack: () -> Unit,
  onUpdateMindful: (Boolean?, Int?, Int?, Boolean?, Boolean?, Int?, Boolean?, Int?) -> Unit,
  onRefreshPermissions: () -> Unit,
) {
  val context = LocalContext.current
  Column(modifier = Modifier.fillMaxSize()) {
    SubPageHeader("Mindful Usage Tools", onBack)
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item {
        SanctuaryCard {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Column(Modifier.weight(1f)) {
                Text("Bedtime Wind-Down", style = MaterialTheme.typography.titleMedium)
                Text(
                  "${FocusTimeUtils.formatMinuteOfDay(settings.bedtimeStartMinute)} – ${FocusTimeUtils.formatMinuteOfDay(settings.bedtimeEndMinute)}",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Switch(
                checked = settings.bedtimeEnabled,
                onCheckedChange = { onUpdateMindful(it, null, null, null, null, null, null, null) },
              )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text("Scheduled Do Not Disturb")
              Switch(
                checked = settings.bedtimeDndEnabled,
                onCheckedChange = { enabled ->
                  if (enabled && !permissions.dndPolicyAccessGranted) {
                    PermissionManager.safeStartSettings(context, PermissionManager.dndPolicySettingsIntent())
                    onRefreshPermissions()
                  } else {
                    onUpdateMindful(null, null, null, enabled, null, null, null, null)
                  }
                },
              )
            }
            Text(
              "Android Grayscale Note: System-wide or per-app grayscale requires Android Digital Wellbeing Bedtime Mode or Developer Options on stock Android.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      item {
        SanctuaryCard {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Column(Modifier.weight(1f)) {
                Text("Mindful Break Reminders", style = MaterialTheme.typography.titleMedium)
                Text("Every ${settings.breakReminderIntervalMinutes} minutes", style = MaterialTheme.typography.bodyMedium)
              }
              Switch(
                checked = settings.breakReminderEnabled,
                onCheckedChange = { onUpdateMindful(null, null, null, null, it, null, null, null) },
              )
            }
            Slider(
              value = settings.breakReminderIntervalMinutes.toFloat(),
              onValueChange = { onUpdateMindful(null, null, null, null, null, (it.toInt() / 15) * 15, null, null) },
              valueRange = 15f..120f,
            )
            Text("App Opening Cooldown: ${settings.appOpeningCooldownSeconds}s", style = MaterialTheme.typography.titleMedium)
            Slider(
              value = settings.appOpeningCooldownSeconds.toFloat(),
              onValueChange = { onUpdateMindful(null, null, null, null, null, null, null, it.toInt()) },
              valueRange = 3f..60f,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun NotificationsSubPage(
  settings: FocusNestSettings,
  permissions: PermissionSnapshot,
  records: List<NotificationRecordEntity>,
  onBack: () -> Unit,
  onToggleFilter: (Boolean) -> Unit,
) {
  val context = LocalContext.current
  Column(modifier = Modifier.fillMaxSize()) {
    SubPageHeader("Notification Management", onBack)
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item {
        SanctuaryCard(highlight = settings.notificationFilterEnabled) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Column(Modifier.weight(1f)) {
                Text("Local Focus Notification Filter", style = MaterialTheme.typography.titleMedium)
                Text(
                  "Mutes distracting notifications during active Focus sessions and logs summaries strictly on-device.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Switch(
                checked = settings.notificationFilterEnabled,
                onCheckedChange = onToggleFilter,
              )
            }
            if (!permissions.notificationListenerEnabled) {
              Button(
                onClick = {
                  PermissionManager.safeStartSettings(
                    context,
                    PermissionManager.notificationListenerSettingsIntent(),
                  )
                },
              ) { Text("Grant Notification Access") }
            }
          }
        }
      }
      item { SectionHeader("Local Notification History", subtitle = "Never transmitted off-device") }
      if (records.isEmpty()) {
        item {
          FocusNestEmptyState(
            icon = Icons.Default.NotificationsPaused,
            title = "No Filtered Notifications Logged",
            message = "Enable Notification Access and Focus Notification Filter to review muted notifications here.",
          )
        }
      } else {
        items(records.take(30), key = { it.id }) { rec ->
          SanctuaryCard {
            Column(modifier = Modifier.padding(14.dp)) {
              Text("${rec.appName} ${if (rec.wasMuted) "• Muted" else ""}", style = MaterialTheme.typography.titleMedium)
              if (rec.titleSummary.isNotBlank()) {
                Text(rec.titleSummary, style = MaterialTheme.typography.bodyMedium)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PermissionsSubPage(
  permissions: PermissionSnapshot,
  onBack: () -> Unit,
  onRefresh: () -> Unit,
) {
  val context = LocalContext.current
  val oem = remember { OemCompatibilityHelper.getGuidance() }
  val notifLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { onRefresh() }
  val cameraLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { onRefresh() }

  Column(modifier = Modifier.fillMaxSize()) {
    SubPageHeader("Permissions & OEM Guide", onBack)
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      item {
        OutlinedButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
          Text("Refresh All Permission States")
        }
      }
      item {
        PermissionRow("Usage Access (UsageStatsManager)", permissions.usageStatsGranted) {
          PermissionManager.safeStartSettings(context, PermissionManager.usageAccessIntent(context))
        }
      }
      item {
        PermissionRow("Accessibility (App & Reels Blocking)", permissions.accessibilityEnabled) {
          PermissionManager.safeStartSettings(context, PermissionManager.accessibilitySettingsIntent())
        }
      }
      item {
        PermissionRow("Local VPN Website Filter", permissions.vpnPrepared && permissions.vpnActive) {
          onRefresh()
        }
      }
      item {
        PermissionRow("Notification Listener", permissions.notificationListenerEnabled) {
          PermissionManager.safeStartSettings(context, PermissionManager.notificationListenerSettingsIntent())
        }
      }
      item {
        PermissionRow("Post Notifications", permissions.postNotificationsGranted) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
          }
        }
      }
      item {
        PermissionRow("Camera (Offline QR/Barcode Scan)", permissions.cameraGranted) {
          cameraLauncher.launch(Manifest.permission.CAMERA)
        }
      }
      item {
        PermissionRow("Unrestricted Battery (Recommended)", permissions.ignoringBatteryOptimizations) {
          PermissionManager.safeStartSettings(context, PermissionManager.batteryOptimizationIntent(context))
        }
      }
      item {
        SanctuaryCard(highlight = oem.hasAggressiveBackgroundKilling) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("${oem.manufacturer} • ${oem.androidVersionLabel}", style = MaterialTheme.typography.titleMedium)
            oem.steps.forEach { step ->
              Text("• $step", style = MaterialTheme.typography.bodyMedium)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PermissionRow(title: String, granted: Boolean, onConfigure: () -> Unit) {
  SanctuaryCard(highlight = granted) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
          if (granted) "Active" else "Not enabled",
          style = MaterialTheme.typography.bodyMedium,
          color = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      OutlinedButton(onClick = onConfigure) { Text("Configure") }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsSubPage(
  settings: FocusNestSettings,
  focusGoal: FocusGoalEntity,
  onBack: () -> Unit,
  onUpdateAppearance: (ThemeMode?, AccentPalette?, Boolean?, String?) -> Unit,
  onUpdateGoals: (Int, Int, Int, Int) -> Unit,
  onBuildEncryptedBackup: suspend (String) -> String,
  onRestoreBackup: suspend (String, String?) -> Result<Int>,
  onDeleteAllData: () -> Unit,
  onShowMessage: (String) -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var passphrase by remember { mutableStateOf("") }
  var pendingRestoreText by remember { mutableStateOf<String?>(null) }
  var showDeleteDialog by remember { mutableStateOf(false) }

  var screenGoal by remember(focusGoal) { mutableIntStateOf(focusGoal.dailyScreenTimeGoalMinutes) }
  var focusGoalMin by remember(focusGoal) { mutableIntStateOf(focusGoal.dailyFocusGoalMinutes) }

  val backupCreator =
    rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
      if (uri != null) {
        scope.launch {
          runCatching {
            val payload = onBuildEncryptedBackup(passphrase)
            context.contentResolver.openOutputStream(uri)?.use { it.write(payload.toByteArray()) }
          }.onSuccess { onShowMessage("Encrypted AES-256-GCM backup saved.") }
            .onFailure { onShowMessage(it.message ?: "Backup failed.") }
        }
      }
    }

  val backupRestorer =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      if (uri != null) {
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        if (text != null) {
          pendingRestoreText = text
        }
      }
    }

  Column(modifier = Modifier.fillMaxSize()) {
    SubPageHeader("Settings & Encrypted Backup", onBack)
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item {
        SanctuaryCard {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Theme & Accent Color", style = MaterialTheme.typography.titleMedium)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              ThemeMode.entries.forEach { mode ->
                FilterChip(
                  selected = settings.themeMode == mode,
                  onClick = { onUpdateAppearance(mode, null, null, null) },
                  label = { Text(mode.label) },
                )
              }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              AccentPalette.entries.forEach { palette ->
                FilterChip(
                  selected = settings.accentPalette == palette,
                  onClick = { onUpdateAppearance(null, palette, null, null) },
                  label = { Text(palette.label) },
                )
              }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
              Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Material You Dynamic Color")
                Switch(
                  checked = settings.dynamicColorEnabled,
                  onCheckedChange = { onUpdateAppearance(null, null, it, null) },
                )
              }
            }
          }
        }
      }

      item {
        SanctuaryCard {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Daily & Weekly Goals", style = MaterialTheme.typography.titleMedium)
            }
            Text("Daily Screen Goal: ${FocusTimeUtils.formatDurationMinutes(screenGoal)}")
            Slider(value = screenGoal.toFloat(), onValueChange = { screenGoal = (it.toInt() / 15) * 15 }, valueRange = 60f..480f)
            Text("Daily Focus Goal: ${FocusTimeUtils.formatDurationMinutes(focusGoalMin)}")
            Slider(value = focusGoalMin.toFloat(), onValueChange = { focusGoalMin = (it.toInt() / 5) * 5 }, valueRange = 15f..240f)
            Button(
              onClick = {
                onUpdateGoals(screenGoal, focusGoalMin, (focusGoalMin / 25).coerceAtLeast(1), focusGoalMin * 7)
              },
              modifier = Modifier.fillMaxWidth(),
            ) { Text("Update Goals") }
          }
        }
      }

      item {
        SanctuaryCard {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Encrypted Local Backup & Restore", style = MaterialTheme.typography.titleMedium)
            }
            OutlinedTextField(
              value = passphrase,
              onValueChange = { passphrase = it },
              label = { Text("Backup Passphrase (min 6 chars)") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                enabled = passphrase.length >= 6,
                onClick = { backupCreator.launch("focusnest_backup_${FocusTimeUtils.todayKey()}.json") },
                modifier = Modifier.weight(1f),
              ) { Text("Export Encrypted") }
              OutlinedButton(
                onClick = { backupRestorer.launch(arrayOf("application/json", "*/*")) },
                modifier = Modifier.weight(1f),
              ) { Text("Restore File") }
            }
          }
        }
      }

      item {
        Button(
          onClick = { showDeleteDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.fillMaxWidth().testTag("settings_delete_all_btn"),
        ) {
          Icon(Icons.Default.DeleteForever, contentDescription = null)
          Spacer(modifier = Modifier.width(6.dp))
          Text("Delete All Local FocusNest Data")
        }
      }
    }
  }

  pendingRestoreText?.let { payload ->
    AlertDialog(
      onDismissRequest = { pendingRestoreText = null },
      title = { Text("Restore Backup") },
      text = {
        OutlinedTextField(
          value = passphrase,
          onValueChange = { passphrase = it },
          label = { Text("Passphrase (if encrypted)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
      },
      confirmButton = {
        Button(
          onClick = {
            scope.launch {
              val res = onRestoreBackup(payload, passphrase.ifBlank { null })
              pendingRestoreText = null
              res.onSuccess { count -> onShowMessage("Restored $count items from backup.") }
                .onFailure { onShowMessage(it.message ?: "Restore failed.") }
            }
          }
        ) { Text("Restore") }
      },
      dismissButton = { TextButton(onClick = { pendingRestoreText = null }) { Text("Cancel") } },
    )
  }

  if (showDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteDialog = false },
      title = { Text("Delete All Local Data?") },
      text = { Text("This removes all saved profiles, focus sessions, usage snapshots, and tokens from this device.") },
      confirmButton = {
        Button(
          onClick = {
            onDeleteAllData()
            showDeleteDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) { Text("Delete Everything") }
      },
      dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } },
    )
  }
}

@Composable
private fun PrivacyAndAboutSubPage(onBack: () -> Unit) {
  Column(modifier = Modifier.fillMaxSize()) {
    SubPageHeader("Privacy, Security & Credits", onBack)
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item {
        SanctuaryCard(highlight = true) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(8.dp))
              Text("FocusNest v1.0.0 • MIT Open Source", style = MaterialTheme.typography.titleMedium)
            }
            Text(
              "100% Free • No Ads • No Trackers • No Cloud Accounts • No Internet Permission.",
              style = MaterialTheme.typography.bodyLarge,
            )
          }
        }
      }
      item {
        SanctuaryCard {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Privacy & Threat Model Summary", style = MaterialTheme.typography.titleMedium)
            Text("• Room SQLite & DataStore Preferences remain local to private app storage.", style = MaterialTheme.typography.bodyMedium)
            Text("• Optional Accessibility & Notification Listener modules never transmit or log screen contents.", style = MaterialTheme.typography.bodyMedium)
            Text("• Optional Local VpnService runs strictly on-device DNS sinkholing with zero remote relays.", style = MaterialTheme.typography.bodyMedium)
            Text("• Portable backups use standard AES-256-GCM + PBKDF2WithHmacSHA256.", style = MaterialTheme.typography.bodyMedium)
          }
        }
      }
      item {
        SanctuaryCard {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Open-Source Credits", style = MaterialTheme.typography.titleMedium)
            Text("• Jetpack Compose, Material 3, Room, DataStore, WorkManager, CameraX, Biometric (Apache 2.0)", style = MaterialTheme.typography.bodyMedium)
            Text("• ZXing Core offline QR & Barcode engine (Apache 2.0)", style = MaterialTheme.typography.bodyMedium)
            Text("• Outfit, Plus Jakarta Sans & JetBrains Mono fonts (SIL Open Font License)", style = MaterialTheme.typography.bodyMedium)
          }
        }
      }
    }
  }
}
