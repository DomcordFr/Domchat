package me.lucid.domchat.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.lucid.domchat.GENERATION_SERVICE_NOTIFICATION_CHANNEL_ID
import me.lucid.domchat.R
import me.lucid.domchat.utils.LogUtil
import org.koin.android.ext.android.inject

class GenerationForegroundService : Service() {
    private val chatService: ChatService by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundSafely()
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundSafely()

        chatService.generationJobs.onEach { jobs ->
            if (jobs.isEmpty()) {
                stopForegroundService()
            }
        }.launchIn(scope)
    }

    private fun startForegroundSafely() {
        val notification = NotificationCompat.Builder(
            this,
            GENERATION_SERVICE_NOTIFICATION_CHANNEL_ID
        )
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_generating_in_background))
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                } else {
                    0
                }
            )
        } catch (e: Exception) {
            LogUtil.e(TAG, "Failed to start foreground notification", e)
            stopSelf()
        }
    }

    private fun stopForegroundService() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "GenerationFGService"
        private const val NOTIFICATION_ID = 9002

        fun start(context: Context) {
            runCatching {
                val intent = Intent(context, GenerationForegroundService::class.java)
                ContextCompat.startForegroundService(context, intent)
            }.onFailure { error ->
                LogUtil.e(TAG, "Failed to start GenerationForegroundService", error)
            }
        }

        fun stop(context: Context) {
            runCatching {
                context.stopService(Intent(context, GenerationForegroundService::class.java))
            }.onFailure { error ->
                LogUtil.e(TAG, "Failed to stop GenerationForegroundService", error)
            }
        }
    }
}
