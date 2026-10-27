package me.lucid.domchat.service

import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager

private const val TAG = "DomChatWorkManager"

fun Context.initializeDomChatWorkManager(): WorkManager? {
    return try {
        if (!WorkManager.isInitialized()) {
            WorkManager.initialize(
                applicationContext,
                Configuration.Builder().build(),
            )
        }
        WorkManager.getInstance(applicationContext)
    } catch (error: NoSuchMethodError) {
        Log.e(TAG, "WorkManager JobScheduler API is unavailable on this device", error)
        null
    } catch (error: Throwable) {
        Log.e(TAG, "WorkManager initialization failed", error)
        null
    }
}

fun Context.initializeAetherWorkManager(): WorkManager? = initializeDomChatWorkManager()

fun Context.workManagerOrNull(): WorkManager? = initializeDomChatWorkManager()
