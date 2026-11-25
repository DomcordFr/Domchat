package me.lucid.domchat.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.lucid.domchat.AppScope
import me.lucid.domchat.R
import me.lucid.domchat.RouteActivity
import me.lucid.domchat.security.InternalIntentAuthenticator
import me.lucid.domchat.WEB_SERVER_NOTIFICATION_CHANNEL_ID
import me.lucid.domchat.data.datastore.SettingsStore
import me.lucid.domchat.web.WebServerPhase
import me.lucid.domchat.web.WebServerManager
import org.koin.android.ext.android.inject

class WebServerService : Service() {

    companion object {
        const val ACTION_START = "me.lucid.domchat.action.WEB_SERVER_START"
        const val ACTION_STOP = "me.lucid.domchat.action.WEB_SERVER_STOP"
        const val EXTRA_PORT = "port"
        private const val NOTIFICATION_ID = 2001
        private const val TAG = "WebServerService"
        private const val WIFI_LOCK_TAG = "domchat:web_server"
        private var stopPersistJob: Job? = null

        fun start(context: android.content.Context, port: Int) {
            val intent = Intent(context, WebServerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_PORT, port)
            }
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) {
                if (isForegroundStartNotAllowed(e)) {
                    Log.w(TAG, "Skipping web server FGS start from a restricted context", e)
                    return
                }
                throw e
            }
        }

        fun stop(context: android.content.Context) {
            runCatching {
                context.stopService(Intent(context, WebServerService::class.java))
            }
        }

        internal fun isForegroundStartNotAllowed(error: Throwable): Boolean {
            if (error::class.simpleName == "ForegroundServiceStartNotAllowedException") {
                return true
            }
            return error is IllegalStateException &&
                error.message?.contains("ForegroundServiceStartNotAllowed", ignoreCase = true) == true
        }
    }

    private val webServerManager: WebServerManager by inject()
    private val settingsStore: SettingsStore by inject()
    private val appScope: AppScope by inject()

    private var nullIntentStartupJob: Job? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var stateObserverJob: Job? = null
    private var wifiLock: WifiManager.WifiLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                nullIntentStartupJob?.cancel()
                nullIntentStartupJob = null
                stopPersistJob?.cancel()
                stopPersistJob = null
                val port = intent.getIntExtra(EXTRA_PORT, 8080)
                if (!startForegroundCompat(buildStartingNotification())) {
                    return START_NOT_STICKY
                }
                startObservingState()
                acquireWifiLock()
                webServerManager.start(port = port)
                return START_STICKY
            }

            ACTION_STOP -> {
                nullIntentStartupJob?.cancel()
                nullIntentStartupJob = null
                webServerManager.stop()
                stopPersistJob?.cancel()
                stopPersistJob = appScope.launch(Dispatchers.IO) {
                    settingsStore.update { settings ->
                        settings.copy(webServerEnabled = false)
                    }
                }
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }

            null -> {
                stopPersistJob?.cancel()
                stopPersistJob = null
                if (!startForegroundCompat(buildStartingNotification())) {
                    return START_NOT_STICKY
                }
                nullIntentStartupJob?.cancel()
                nullIntentStartupJob = serviceScope.launch {
                    val settings = settingsStore.settingsFlowRaw.first()
                    if (settings.webServerEnabled) {
                        startObservingState()
                        acquireWifiLock()
                        webServerManager.start(port = settings.webServerPort)
                    } else {
                        releaseWifiLock()
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                }
                return START_STICKY
            }
        }
        return START_STICKY
    }

    private fun startForegroundCompat(notification: android.app.Notification): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    notification,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    } else {
                        0
                    }
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Unable to promote web server to a foreground service", e)
            stopSelf()
            false
        }
    }

    private fun startObservingState() {
        if (stateObserverJob != null) return

        stateObserverJob = serviceScope.launch {
            var hasSeenActivePhase = false
            webServerManager.state.collect { state ->
                when (state.phase) {
                    WebServerPhase.Starting -> {
                        hasSeenActivePhase = true
                        acquireWifiLock()
                        updateNotification(buildStartingNotification())
                    }

                    WebServerPhase.Running -> {
                        hasSeenActivePhase = true
                        acquireWifiLock()
                        updateNotification(buildRunningNotification(state.preferredUrl))
                    }

                    WebServerPhase.Stopping -> {
                        hasSeenActivePhase = true
                        updateNotification(buildStoppingNotification())
                    }

                    WebServerPhase.Error -> {
                        hasSeenActivePhase = true
                        releaseWifiLock()
                        updateNotification(buildErrorNotification(state.error ?: getString(R.string.notification_web_server_error)))
                        stopPersistJob?.cancel()
                        stopPersistJob = appScope.launch(Dispatchers.IO) {
                            settingsStore.update { settings -> settings.copy(webServerEnabled = false) }
                        }
                        stopForeground(STOP_FOREGROUND_DETACH)
                        stopSelf()
                    }

                    WebServerPhase.Idle -> {
                        if (hasSeenActivePhase) {
                            releaseWifiLock()
                            ServiceCompat.stopForeground(this@WebServerService, ServiceCompat.STOP_FOREGROUND_REMOVE)
                            stopSelf()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        nullIntentStartupJob?.cancel()
        nullIntentStartupJob = null
        webServerManager.stop()
        stateObserverJob?.cancel()
        stateObserverJob = null
        releaseWifiLock()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun updateNotification(notification: android.app.Notification) {
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildStartingNotification(): android.app.Notification {
        return NotificationCompat.Builder(this, WEB_SERVER_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notification_web_server_title))
            .setContentText(getString(R.string.notification_web_server_starting))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun buildStoppingNotification(): android.app.Notification {
        return NotificationCompat.Builder(this, WEB_SERVER_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notification_web_server_title))
            .setContentText(getString(R.string.notification_web_server_stopping))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun buildRunningNotification(url: String?): android.app.Notification {
        val stopIntent = Intent(this, WebServerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val openIntent = Intent(this, RouteActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("webServerSettings", true)
            InternalIntentAuthenticator.signInternalIntent(this@WebServerService, this)
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            1,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, WEB_SERVER_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notification_web_server_title))
            .setContentText(url ?: getString(R.string.notification_web_server_running))
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(0, getString(R.string.setting_page_web_server_stop), stopPendingIntent)
            .build()
    }

    private fun buildErrorNotification(error: String): android.app.Notification {
        return NotificationCompat.Builder(this, WEB_SERVER_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notification_web_server_error))
            .setContentText(error)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .build()
    }

    private fun acquireWifiLock() {
        val existingLock = wifiLock
        if (existingLock?.isHeld == true) {
            return
        }

        runCatching {
            val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            val lock = existingLock ?: wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, WIFI_LOCK_TAG).apply {
                setReferenceCounted(false)
            }
            if (!lock.isHeld) {
                lock.acquire()
            }
            wifiLock = lock
        }.onFailure {
            Log.w(TAG, "Failed to acquire Wi-Fi lock", it)
        }
    }

    private fun releaseWifiLock() {
        runCatching {
            wifiLock?.takeIf { it.isHeld }?.release()
        }.onFailure {
            Log.w(TAG, "Failed to release Wi-Fi lock", it)
        }
    }
}
