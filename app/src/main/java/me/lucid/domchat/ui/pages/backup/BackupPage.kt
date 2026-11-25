package me.lucid.domchat.ui.pages.backup

import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.components.ui.DomChatGlassPillButton
import me.lucid.domchat.utils.PermissionChecker
import me.lucid.domchat.utils.BoundedStreams
import me.lucid.domchat.utils.BoundedStreams.copyBounded

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.SystemUpdateAlt
import me.lucid.domchat.ui.components.ui.ToastType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.lucid.domchat.R
import me.lucid.domchat.data.sync.BackupContentOption
import me.lucid.domchat.ui.components.nav.BackButton
import me.lucid.domchat.ui.components.nav.OneUITopAppBar
import me.lucid.domchat.ui.components.ui.DomChatDestructiveConfirmDialog
import me.lucid.domchat.ui.context.LocalToaster
import me.lucid.domchat.ui.pages.setting.components.SettingGroupItem
import me.lucid.domchat.ui.pages.setting.components.SettingsGroup
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.domchat.utils.fileSizeToString
import okio.buffer
import okio.sink
import okio.source
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class BackupTab(
    val routeValue: String,
    val icon: ImageVector,
    val titleRes: Int,
) {
    WebDav("webdav", Icons.Rounded.CloudSync, R.string.backup_page_title),
    Local("local", Icons.Rounded.Folder, R.string.backup_page_title);

    companion object {
        fun fromRoute(route: String): BackupTab {
            return entries.firstOrNull { it.routeValue == route } ?: Local
        }
    }
}

@Composable
fun BackupPage(
    initialTab: BackupTab = BackupTab.Local,
    vm: BackupVM = koinViewModel(),
) {
    val toaster = LocalToaster.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val settings by vm.settings.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var isExporting by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    var showExportContentDialog by remember { mutableStateOf(false) }
    var selectedExportContent by remember { mutableStateOf<Set<BackupContentOption>?>(null) }
    var selectedExportKeyIds by remember { mutableStateOf<Set<kotlin.uuid.Uuid>?>(null) }

    var restoreResult by remember { mutableStateOf<me.lucid.domchat.data.sync.WebdavSync.RestoreResult?>(null) }

    // Permission handling after restore
    var pendingFeatureAccess by remember {
        mutableStateOf(PermissionChecker.MissingFeatureAccess())
    }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        scope.launch {
            val missing = PermissionChecker.getMissingFeatureAccess(context, vm.getAssistantsSnapshot())
            if (missing.specialAccesses.isNotEmpty()) {
                pendingFeatureAccess = PermissionChecker.MissingFeatureAccess(
                    specialAccesses = missing.specialAccesses
                )
                showPermissionDialog = true
            } else {
                showRestartDialog = true
            }
        }
    }

    val specialAccessLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        showRestartDialog = true
    }

    var importType by remember { mutableStateOf("local") }
    var pendingLocalRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let { targetUri ->
            scope.launch {
                isExporting = true
                runCatching {
                    val exportFile = vm.exportToFile(
                        selectedContent = selectedExportContent,
                        selectedKeyIds = selectedExportKeyIds,
                    )

                    try {
                        withContext(Dispatchers.IO) {
                            val outputStream = context.contentResolver.openOutputStream(targetUri)
                                ?: throw java.io.IOException("Unable to open destination")
                            outputStream.use { out ->
                                exportFile.source().buffer().inputStream().use { inputStream ->
                                    inputStream.copyTo(out)
                                }
                            }
                        }

                        toaster.show(
                            context.getString(R.string.backup_page_backup_success),
                            type = ToastType.Success
                        )
                    } finally {
                        withContext(Dispatchers.IO) {
                            if (exportFile.exists()) {
                                exportFile.delete()
                            }
                        }
                    }
                }.onFailure { e ->
                    e.printStackTrace()
                    toaster.show(
                        context.getString(R.string.backup_page_backup_failed, e.message ?: ""),
                        type = ToastType.Error
                    )
                }
                isExporting = false
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { sourceUri ->
            if (importType == "local") {
                pendingLocalRestoreUri = sourceUri
                return@rememberLauncherForActivityResult
            }
            scope.launch {
                isRestoring = true
                runCatching {
                    when (importType) {
                        "chatbox" -> {
                            val tempFile =
                                File(context.cacheDir, "temp_chatbox_${System.currentTimeMillis()}.json")

                            try {
                                withContext(Dispatchers.IO) {
                                    val input = context.contentResolver.openInputStream(sourceUri)
                                        ?: throw java.io.IOException("Unable to open source chatbox file")
                                    input.use { inputStream ->
                                        tempFile.sink().buffer().outputStream().use { outputStream ->
                                            inputStream.copyBounded(outputStream, BoundedStreams.MAX_ARCHIVE_TOTAL_BYTES)
                                        }
                                    }
                                }
                                vm.restoreFromChatBox(tempFile)
                            } finally {
                                withContext(Dispatchers.IO) {
                                    tempFile.delete()
                                }
                            }
                        }
                        "cherry" -> {
                            val tempFile =
                                File(context.cacheDir, "temp_cherry_${System.currentTimeMillis()}.zip")

                            try {
                                withContext(Dispatchers.IO) {
                                    val input = context.contentResolver.openInputStream(sourceUri)
                                        ?: throw java.io.IOException("Unable to open source cherry studio file")
                                    input.use { inputStream ->
                                        tempFile.sink().buffer().outputStream().use { outputStream ->
                                            inputStream.copyBounded(outputStream, BoundedStreams.MAX_ARCHIVE_TOTAL_BYTES)
                                        }
                                    }
                                }
                                vm.restoreFromCherryStudio(tempFile)
                            } finally {
                                withContext(Dispatchers.IO) {
                                    tempFile.delete()
                                }
                            }
                        }
                    }

                    toaster.show(
                        context.getString(R.string.backup_page_restore_success),
                        type = ToastType.Success
                    )
                }.onFailure { e ->
                    e.printStackTrace()
                    toaster.show(
                        context.getString(R.string.backup_page_restore_failed, e.message ?: ""),
                        type = ToastType.Error
                    )
                }
                isRestoring = false
            }
        }
    }

    DomChatDestructiveConfirmDialog(
        visible = pendingLocalRestoreUri != null,
        title = stringResource(R.string.backup_page_restore_overwrite_title),
        consequence = stringResource(R.string.backup_page_restore_overwrite_consequence),
        confirmLabel = stringResource(R.string.backup_page_restore_now),
        onDismiss = { pendingLocalRestoreUri = null },
        onConfirm = {
            pendingLocalRestoreUri?.let { sourceUri ->
                pendingLocalRestoreUri = null
                scope.launch {
                    isRestoring = true
                    runCatching {
                        val tempFile =
                            File(context.cacheDir, "temp_restore_${System.currentTimeMillis()}.zip")
                        try {
                            withContext(Dispatchers.IO) {
                                val inputStream = context.contentResolver.openInputStream(sourceUri)
                                    ?: throw java.io.IOException("Unable to open source backup file")
                                inputStream.use { input ->
                                    tempFile.sink().buffer().outputStream().use { outputStream ->
                                        input.copyBounded(outputStream, BoundedStreams.MAX_ARCHIVE_TOTAL_BYTES)
                                    }
                                }
                            }
                            val result = vm.restoreFromLocalFile(tempFile)
                            restoreResult = result
                            toaster.show(
                                context.getString(R.string.backup_page_restore_success),
                                type = ToastType.Success
                            )
                        } finally {
                            withContext(Dispatchers.IO) {
                                if (tempFile.exists()) tempFile.delete()
                            }
                        }
                        val missing = PermissionChecker.getMissingFeatureAccess(
                            context,
                            vm.getAssistantsSnapshot()
                        )
                        if (!missing.isEmpty) {
                            pendingFeatureAccess = missing
                            showPermissionDialog = true
                        } else {
                            showRestartDialog = true
                        }
                    }.onFailure { e ->
                        e.printStackTrace()
                        toaster.show(
                            context.getString(R.string.backup_page_restore_failed, e.message ?: ""),
                            type = ToastType.Error
                        )
                    }
                    isRestoring = false
                }
            }
        },
    )

    if (showExportContentDialog) {
        ExportContentSelectionDialog(
            providers = settings.providers,
            onDismiss = { showExportContentDialog = false },
            onConfirm = { selectedContent, selectedKeys ->
                showExportContentDialog = false
                selectedExportContent = selectedContent
                selectedExportKeyIds = selectedKeys
                val timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                val fileName = if (selectedContent == null) "Domchat_all_in_one_$timestamp.zip" else "Domchat_backup_$timestamp.zip"
                createDocumentLauncher.launch(fileName)
            }
        )
    }

    Scaffold(
        topBar = {
            OneUITopAppBar(
                title = "Backup and Restore",
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    BackButton()
                }
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item {
                SettingsGroup(
                    title = "Backup and Restore",
                    horizontalPadding = 16.dp,
                    titleStartPadding = 16.dp,
                ) {
                    SettingGroupItem(
                        title = stringResource(R.string.backup_page_all_in_one_export),
                        subtitle = if (isExporting && selectedExportContent == null) {
                            stringResource(R.string.backup_page_exporting)
                        } else {
                            stringResource(R.string.backup_page_all_in_one_export_desc)
                        },
                        icon = {
                            if (isExporting && selectedExportContent == null) {
                                CircularWavyProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Rounded.Inventory2, null)
                            }
                        },
                        onClick = {
                            if (!isExporting) {
                                selectedExportContent = null
                                selectedExportKeyIds = null
                                val timestamp = LocalDateTime.now()
                                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                                createDocumentLauncher.launch("Domchat_all_in_one_$timestamp.zip")
                            }
                        }
                    )
                    SettingGroupItem(
                        title = stringResource(R.string.backup_page_custom_backup_export),
                        subtitle = if (isExporting && selectedExportContent != null) {
                            stringResource(R.string.backup_page_exporting)
                        } else {
                            stringResource(R.string.backup_page_custom_backup_export_desc)
                        },
                        icon = {
                            if (isExporting && selectedExportContent != null) {
                                CircularWavyProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Rounded.FileUpload, null)
                            }
                        },
                        onClick = {
                            if (!isExporting) {
                                showExportContentDialog = true
                            }
                        }
                    )
                    SettingGroupItem(
                        title = stringResource(R.string.backup_page_local_backup_import),
                        subtitle = if (isRestoring && importType == "local") {
                            stringResource(R.string.backup_page_importing)
                        } else {
                            stringResource(R.string.backup_page_import_desc)
                        },
                        icon = {
                            if (isRestoring && importType == "local") {
                                CircularWavyProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Rounded.SystemUpdateAlt, null)
                            }
                        },
                        onClick = {
                            if (!isRestoring) {
                                importType = "local"
                                openDocumentLauncher.launch(
                                    arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream", "*/*")
                                )
                            }
                        }
                    )
                }
            }

            item {
                SettingsGroup(
                    title = stringResource(R.string.backup_page_import_from_other_app),
                    horizontalPadding = 16.dp,
                    titleStartPadding = 16.dp,
                ) {
                    SettingGroupItem(
                        title = stringResource(R.string.backup_page_import_from_chatbox),
                        subtitle = stringResource(R.string.backup_page_import_chatbox_desc),
                        icon = {
                            if (isRestoring && importType == "chatbox") {
                                CircularWavyProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Rounded.SystemUpdateAlt, null)
                            }
                        },
                        onClick = {
                            if (!isRestoring) {
                                importType = "chatbox"
                                openDocumentLauncher.launch(arrayOf("application/json", "text/plain"))
                            }
                        }
                    )
                    SettingGroupItem(
                        title = stringResource(R.string.backup_page_import_from_cherry_studio),
                        subtitle = stringResource(R.string.backup_page_import_cherry_studio_desc),
                        icon = {
                            if (isRestoring && importType == "cherry") {
                                CircularWavyProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Rounded.Folder, null)
                            }
                        },
                        onClick = {
                            if (!isRestoring) {
                                importType = "cherry"
                                openDocumentLauncher.launch(
                                    arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream")
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    // Permission explanation dialog
    if (showPermissionDialog) {
        AlertDialog(
            containerColor = Color(0xFF000000),
            shape = AppShapes.SettingDialog,
            modifier = Modifier.border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, AppShapes.SettingDialog),
            titleContentColor = Color(0xFFF2F2F7),
            textContentColor = Color(0xFFF2F2F7),
            onDismissRequest = {
                showPermissionDialog = false
                showRestartDialog = true
            },
            title = { Text(stringResource(R.string.backup_restore_permissions_required)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.backup_restore_permissions_message))
                    PermissionChecker.getFeatureAccessDescriptions(pendingFeatureAccess).forEach { description ->
                        Text("- $description", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        stringResource(R.string.backup_restore_permissions_follow_up),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFFFFF),
                        contentColor = Color(0xFF000000)
                    ),
                    onClick = {
                        showPermissionDialog = false
                        when {
                            pendingFeatureAccess.runtimePermissions.isNotEmpty() -> {
                                permissionLauncher.launch(pendingFeatureAccess.runtimePermissions.toTypedArray())
                            }
                            pendingFeatureAccess.specialAccesses.isNotEmpty() -> {
                                val nextAccess = pendingFeatureAccess.specialAccesses.firstOrNull()
                                    ?: return@Button
                                specialAccessLauncher.launch(
                                    PermissionChecker.createSpecialAccessIntent(nextAccess)
                                )
                            }
                            else -> {
                                showRestartDialog = true
                            }
                        }
                    }
                ) {
                    Text(
                        if (pendingFeatureAccess.runtimePermissions.isNotEmpty()) {
                            stringResource(R.string.backup_restore_grant_permissions)
                        } else {
                            stringResource(R.string.open_settings)
                        }
                    )
                }
            },
            dismissButton = {
                DomChatGlassPillButton(
                    text = stringResource(R.string.backup_restore_skip),
                    containerColor = Color(0xFF000000),
                    onClick = {
                        showPermissionDialog = false
                        showRestartDialog = true
                    }
                )
            }
        )
    }

    // Restart dialog
    if (showRestartDialog) {
        val result = restoreResult
        BackupDialog(
            result = result,
            onConfirm = {
                vm.restartApp(context)
            }
        )
    }
}

@Composable
private fun BackupDialog(
    result: me.lucid.domchat.data.sync.WebdavSync.RestoreResult?,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = Color(0xFF000000),
        shape = AppShapes.SettingDialog,
        modifier = Modifier.border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, AppShapes.SettingDialog),
        title = { Text(stringResource(R.string.backup_page_restart_app)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.backup_page_restart_desc))

                result?.let {
                    if (it.sanitization.skippedRows > 0 || it.settingsCleanup.totalIssuesFixed > 0 || it.settingsCleanup.unsupportedZipEntriesBytes > 0) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF000000).copy(alpha = 0.55f),
                                contentColor = Color(0xFFFF6B6B)
                            )
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = stringResource(R.string.backup_restore_report_title),
                                    style = MaterialTheme.typography.labelMedium
                                )
                                if (it.sanitization.skippedRows > 0) {
                                    Text(
                                        text = stringResource(
                                            R.string.backup_restore_report_removed_items,
                                            it.sanitization.skippedRows
                                        )
                                    )
                                }
                                if (it.settingsCleanup.totalIssuesFixed > 0) {
                                    Text(
                                        text = stringResource(
                                            R.string.backup_restore_report_fixed_settings,
                                            it.settingsCleanup.totalIssuesFixed
                                        )
                                    )
                                }
                                if (it.settingsCleanup.unsupportedZipEntriesBytes > 0) {
                                    Text(
                                        text = stringResource(
                                            R.string.backup_restore_report_cleaned_data,
                                            it.settingsCleanup.unsupportedZipEntriesBytes.fileSizeToString()
                                        )
                                    )
                                }
                            }
                        }
                    }
                    val envTargets = it.workspacesMetadata.filter { meta -> meta.hasRootfs }
                    if (envTargets.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF000000).copy(alpha = 0.55f),
                                contentColor = Color(0xFFF2F2F7)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.backup_restore_workspaces_env_title),
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    text = stringResource(
                                        R.string.backup_restore_workspaces_env_desc,
                                        envTargets.size
                                    ),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            DomChatGlassPillButton(
                text = stringResource(R.string.backup_page_restart_app),
                onClick = onConfirm
            )
        },
    )
}
