package me.lucid.domchat.ui.pages.setting

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SystemUpdateAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.lucid.domchat.BuildConfig
import me.lucid.domchat.R
import me.lucid.domchat.ui.components.nav.BackButton
import me.lucid.domchat.ui.components.settings.DomChatAboutContent
import me.lucid.domchat.ui.components.settings.DomChatAboutItem
import me.lucid.domchat.ui.components.ui.HapticSwitch
import me.lucid.domchat.ui.components.ui.ToastType
import me.lucid.domchat.ui.components.ui.UpdateDialog
import me.lucid.domchat.ui.context.LocalToaster
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.utils.UiState
import me.lucid.domchat.utils.UpdateChecker
import me.lucid.domchat.utils.UpdateInfo
import me.lucid.domchat.utils.Version
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun SettingAboutPage(settingVm: SettingVM = koinViewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = rememberPremiumHaptics()
    val toaster = LocalToaster.current
    val updateChecker = koinInject<UpdateChecker>()
    val settings by settingVm.settings.collectAsStateWithLifecycle()
    val currentVersionName = BuildConfig.VERSION_NAME

    var isCheckingForUpdate by remember { mutableStateOf(false) }
    var manualUpdateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var showManualUpdateDialog by remember { mutableStateOf(false) }

    fun checkForUpdateManually(openDialogRegardless: Boolean = false, notifyUpToDate: Boolean = true) {
        if (isCheckingForUpdate) return
        isCheckingForUpdate = true
        scope.launch {
            try {
                updateChecker.checkUpdate().collect { state ->
                    when (state) {
                        is UiState.Idle -> Unit
                        is UiState.Loading -> {
                            isCheckingForUpdate = true
                        }
                        is UiState.Success -> {
                            isCheckingForUpdate = false
                            val info = state.data
                            manualUpdateInfo = info
                            val latestVersion = Version(info.version)
                            val currentVersion = Version(currentVersionName)
                            if (openDialogRegardless || latestVersion > currentVersion) {
                                showManualUpdateDialog = true
                            } else if (notifyUpToDate) {
                                toaster.show(
                                    message = "You're on the latest version (v$currentVersionName)",
                                    type = ToastType.Success
                                )
                            }
                        }
                        is UiState.Error -> {
                            isCheckingForUpdate = false
                            toaster.show(
                                message = state.error.message?.takeIf { it.isNotBlank() } ?: "Failed to check for updates",
                                type = ToastType.Error
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is kotlin.coroutines.cancellation.CancellationException) throw e
                isCheckingForUpdate = false
                toaster.show(
                    message = e.message?.takeIf { it.isNotBlank() } ?: "Failed to check for updates",
                    type = ToastType.Error
                )
            }
        }
    }

    // Automatically check on mount to populate the latest version line
    LaunchedEffect(Unit) {
        checkForUpdateManually(openDialogRegardless = false, notifyUpToDate = false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.setting_page_about)) },
                navigationIcon = { BackButton() },
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            DomChatAboutContent(
                appName = stringResource(R.string.app_name),
                versionName = currentVersionName,
                darkTheme = LocalDarkMode.current,
                platformTitle = "Android Version",
                platformSubtitle = "${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})",
                platformIcon = Icons.Rounded.Android,
                deviceSubtitle = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
                deviceIcon = Icons.Rounded.PhoneAndroid,
                architectureSubtitle = android.os.Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown",
                architectureIcon = Icons.Rounded.Memory,
                onSourceCode = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/DomcordFr/Domchat"),
                        ),
                    )
                },
                extraProjectContent = {
                    // 1. Check for Updates switch card (full multiline description)
                    DomChatAboutItem(
                        icon = Icons.Rounded.Refresh,
                        title = stringResource(R.string.setting_display_check_updates_title),
                        subtitle = stringResource(R.string.setting_display_check_updates_desc),
                        subtitleMaxLines = Int.MAX_VALUE,
                        darkTheme = LocalDarkMode.current,
                        trailingContent = {
                            HapticSwitch(
                                checked = settings.displaySetting.checkForUpdates,
                                onCheckedChange = { checked ->
                                    settingVm.updateDisplaySetting(settings.displaySetting.copy(checkForUpdates = checked))
                                }
                            )
                        }
                    )

                    // 2. Version Update Hero Grid Card (Option C)
                    Surface(
                        color = Color(0xFF000000),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            0.5.dp,
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.22f),
                                    Color.White.copy(alpha = 0.05f),
                                    Color.Transparent
                                )
                            )
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            val info = manualUpdateInfo
                            val isNewer = info != null && Version(info.version) > Version(currentVersionName)

                            // Header Row: Icon, Title & Status Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.size(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.SystemUpdateAlt,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                    }
                                    Text(
                                        text = "Version Update",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFFF2F2F7)
                                    )
                                }

                                val badgeText = when {
                                    isCheckingForUpdate -> "Checking..."
                                    isNewer -> "Update Available"
                                    else -> "Up to Date"
                                }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFF000000),
                                    border = if (isNewer) {
                                        BorderStroke(0.5.dp, Color(0xFF34C759).copy(alpha = 0.45f))
                                    } else {
                                        BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                                    }
                                ) {
                                    Text(
                                        text = badgeText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (isNewer) Color(0xFF34C759) else Color.White.copy(alpha = 0.70f),
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Symmetrical Metric Tiles
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Installed Tile
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF000000),
                                    border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = "INSTALLED",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = Color(0xFF8E8E93)
                                        )
                                        Text(
                                            text = "v$currentVersionName",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 16.sp
                                            ),
                                            color = Color.White
                                        )
                                    }
                                }

                                // Latest Release Tile
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF000000),
                                    border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = "LATEST RELEASE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = Color(0xFF8E8E93)
                                        )
                                        val latestVerText = when {
                                            isCheckingForUpdate -> "Checking..."
                                            info != null -> "v${info.version}"
                                            else -> "v$currentVersionName"
                                        }
                                        Text(
                                            text = latestVerText,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 16.sp
                                            ),
                                            color = if (isNewer) Color(0xFF34C759) else Color.White
                                        )
                                    }
                                }
                            }

                            // Dedicated Action Button (12.dp radius, Black BG, Glassy Border, Download Icon)
                            val installBtnInteraction = remember { MutableInteractionSource() }
                            val isInstallPressed by installBtnInteraction.collectIsPressedAsState()
                            val installScale by animateFloatAsState(
                                targetValue = if (isInstallPressed) 0.96f else 1f,
                                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
                                label = "about_install_btn_press"
                            )

                            Button(
                                onClick = {
                                    haptics.perform(HapticPattern.Pop)
                                    if (info != null) {
                                        showManualUpdateDialog = true
                                    } else {
                                        checkForUpdateManually(openDialogRegardless = true, notifyUpToDate = false)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF000000),
                                    contentColor = Color.White
                                ),
                                interactionSource = installBtnInteraction,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .graphicsLayer {
                                        scaleX = installScale
                                        scaleY = installScale
                                    }
                            ) {
                                if (isCheckingForUpdate) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Checking...",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Download,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    val btnText = when {
                                        info != null && Version(info.version) > Version(currentVersionName) ->
                                            "Install Update"
                                        else ->
                                            "Install Latest"
                                    }
                                    Text(
                                        text = btnText,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                },
                onVersionLongPress = {
                    updateChecker.forceUpdateCheck()
                    toaster.show(
                        message = "Update banner forced for testing.",
                        type = ToastType.Info,
                    )
                },
                onHaptic = { haptics.perform(HapticPattern.Pop) },
            )
        }
    }

    val currentManualUpdate = manualUpdateInfo
    if (showManualUpdateDialog && currentManualUpdate != null) {
        UpdateDialog(
            info = currentManualUpdate,
            updateChecker = updateChecker,
            onDismiss = {
                showManualUpdateDialog = false
            },
            onLater = {
                updateChecker.dismissVersion(context, currentManualUpdate.version)
                showManualUpdateDialog = false
            },
            onIgnore = {
                updateChecker.ignoreUpdate(context, currentManualUpdate.version)
                showManualUpdateDialog = false
            }
        )
    }
}
