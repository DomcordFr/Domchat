package me.lucid.domchat.service

import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager

private const val TAG = "DomChatWorkManager"

fun Context.initializeDomChatWorkManager(): WorkManager? {
    return try {
        if (!WorkManager.isInitialized()) {
            val provider = applicationContext as? Configuration.Provider
            val config = provider?.workManagerConfiguration ?: Configuration.Builder().build()
            try {
                WorkManager.initialize(applicationContext, config)
            } catch (ignored: IllegalStateException) {
                // Already initialized concurrently
            }
        }
        WorkManager.getInstance(applicationContext)
    } catch (error: NoSuchMethodError) {
        Log.e(TAG, "WorkManager JobScheduler API is unavailable on this device", error)
        null
    } catch (error: IllegalStateException) {
        try {
            WorkManager.getInstance(applicationContext)
        } catch (t: Throwable) {
            Log.e(TAG, "WorkManager getInstance failed", t)
            null
        }
    } catch (error: Throwable) {
        Log.e(TAG, "WorkManager initialization failed", error)
        null
    }
}

fun Context.initializeAetherWorkManager(): WorkManager? = initializeDomChatWorkManager()

fun Context.workManagerOrNull(): WorkManager? = initializeDomChatWorkManager()
